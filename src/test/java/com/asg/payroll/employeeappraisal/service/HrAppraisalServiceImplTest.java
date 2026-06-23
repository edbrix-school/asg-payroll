package com.asg.payroll.employeeappraisal.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.service.PrintService;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalActionRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalDtlRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRecalculationRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRequest;
import com.asg.payroll.employeeappraisal.entity.HrAppraisalDtl;
import com.asg.payroll.employeeappraisal.entity.HrAppraisalHdr;
import com.asg.payroll.employeeappraisal.enums.ActionType;
import com.asg.payroll.employeeappraisal.repository.HrAppraisalDtlRepository;
import com.asg.payroll.employeeappraisal.repository.HrAppraisalHdrRepository;
import com.asg.payroll.salarydetails.repository.HrEmployeeSalaryMasterRepository;
import com.asg.payroll.employeeappraisal.repository.HrPayrollVarAlwdedDtlRepository;
import com.asg.payroll.exceptions.ResourceNotFoundException;
import com.asg.payroll.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrAppraisalServiceImplTest {

    @Mock
    private HrAppraisalHdrRepository hdrRepository;

    @Mock
    private HrAppraisalDtlRepository dtlRepository;

    @Mock
    private HrEmployeeSalaryMasterRepository salaryMasterRepository;

    @Mock
    private HrPayrollVarAlwdedDtlRepository payrollVarDtlRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private DocumentSearchService documentSearchService;

    @Mock
    private DocumentDeleteService documentDeleteService;

    @Mock
    private LoggingService loggingService;

    @Mock
    private PrintService printService;

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private HrAppraisalService self;

    @Mock
    private LovDataService lovDataService;

    @Mock
    private Connection mockConnection;

    @Mock
    private CallableStatement mockCallableStatement;

    @InjectMocks
    private HrAppraisalServiceImpl service;

    private HrAppraisalHdr mockHdr;
    private HrAppraisalDtl mockDtl;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "self", self);
        mockHdr = new HrAppraisalHdr();
        mockHdr.setTransactionPoid(1L);
        mockHdr.setDocRef("APR-001");
        mockHdr.setDescription("Test Appraisal");
        mockHdr.setPeriodFrom(LocalDate.of(2024, 1, 1));
        mockHdr.setAppraisalBasicPercent(BigDecimal.valueOf(60));
        mockHdr.setAppraisalFixedPercent(BigDecimal.valueOf(40));
        mockHdr.setApprovedBy("Manager");

        mockDtl = new HrAppraisalDtl();
        mockDtl.setTransactionPoid(1L);
        mockDtl.setDetRowId(1L);
        mockDtl.setEmployeePoid(100L);
    }

    @Test
    void listAppraisals_Success() {
        RawSearchResult rawResult = new RawSearchResult(List.of(new HashMap<>()), new HashMap<>(), 1L);
        when(documentSearchService.resolveOperator(any())).thenReturn("AND");
        when(documentSearchService.resolveIsDeleted(any())).thenReturn("N");
        when(documentSearchService.resolveDateFilters(any(), any(), any(), any())).thenReturn(List.of());
        when(documentSearchService.search(any(), any(), any(), any(), any(), any(), any())).thenReturn(rawResult);

        Map<String, Object> result = service.listAppraisals("DOC123", new FilterRequestDto("AND", "N", List.of()), PageRequest.of(0, 10), null, null);

        assertNotNull(result);
        verify(documentSearchService).search(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getAppraisalById_Success() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        Map<String, Object> result = service.getAppraisalById(1L);

        assertNotNull(result);
        assertEquals(mockHdr, result.get("header"));
        assertNotNull(result.get("details"));
        assertNotNull(result.get("totals"));
    }

    @Test
    void getAppraisalById_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getAppraisalById(1L));
    }

    @Test
    void getFilteredDetails_ReturnsTotals() {
        Map<String, Object> result = service.getFilteredDetails(1L, null, null, null, null);

        assertNotNull(result);
        assertNotNull(result.get("details"));
        assertNotNull(result.get("totals"));
        assertNotNull(result.get("totalRecords"));
    }

    @Test
    void createAppraisal_Success() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("New Appraisal");
        request.setPeriodFrom(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.valueOf(60));
        request.setAppraisalFixedPercent(BigDecimal.valueOf(40));
        request.setDetails(new ArrayList<>());

        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);
        when(self.getAppraisalById(1L)).thenReturn(Map.of("header", mockHdr));

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            Map<String, Object> result = service.createAppraisal(request);

            assertNotNull(result);
            verify(hdrRepository).saveAndFlush(any());
        }
    }

    @Test
    void createAppraisal_NullRequest_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> service.createAppraisal(null));
    }

    @Test
    void createAppraisal_NullDescription_ThrowsValidationException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setPeriodFrom(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);

        assertThrows(ValidationException.class, () -> service.createAppraisal(request));
    }

    @Test
    void createAppraisal_BlankDescription_ThrowsValidationException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("   ");
        request.setPeriodFrom(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);

        assertThrows(ValidationException.class, () -> service.createAppraisal(request));
    }

    @Test
    void createAppraisal_MissingPeriodFrom_ThrowsValidationException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);

        assertThrows(ValidationException.class, () -> service.createAppraisal(request));
    }

    @Test
    void createAppraisal_MissingAppraisalBasicPercent_ThrowsValidationException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setAppraisalFixedPercent(BigDecimal.valueOf(40));

        assertThrows(ValidationException.class, () -> service.createAppraisal(request));
    }

    @Test
    void createAppraisal_MissingAppraisalFixedPercent_ThrowsValidationException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.valueOf(60));

        assertThrows(ValidationException.class, () -> service.createAppraisal(request));
    }

    @Test
    void updateAppraisal_Success() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Updated Appraisal");
        request.setPeriodFrom(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.valueOf(60));
        request.setAppraisalFixedPercent(BigDecimal.valueOf(40));
        request.setDetails(new ArrayList<>());

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(self.getAppraisalById(1L)).thenReturn(Map.of("header", mockHdr));

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            Map<String, Object> result = service.updateAppraisal(1L, request);

            assertNotNull(result);
            verify(hdrRepository).save(any());
        }
    }

    @Test
    void updateAppraisal_NullRequest_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> service.updateAppraisal(1L, null));
    }

    @Test
    void updateAppraisal_BlankDescription_ThrowsValidationException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("");
        request.setPeriodFrom(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);

        assertThrows(ValidationException.class, () -> service.updateAppraisal(1L, request));
    }

    @Test
    void updateAppraisal_MissingAppraisalFixedPercent_ThrowsValidationException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.TEN);

        assertThrows(ValidationException.class, () -> service.updateAppraisal(1L, request));
    }

    @Test
    void updateAppraisal_NotFound_ThrowsException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);

        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateAppraisal(1L, request));
    }

    // ── upsertDetails branches ────────────────────────────────────────────

    private HrAppraisalRequest validRequest(List<HrAppraisalDtlRequest> details) {
        HrAppraisalRequest r = new HrAppraisalRequest();
        r.setDescription("Test");
        r.setPeriodFrom(LocalDate.of(2024, 1, 1));
        r.setAppraisalBasicPercent(BigDecimal.valueOf(60));
        r.setAppraisalFixedPercent(BigDecimal.valueOf(40));
        r.setDetails(details);
        return r;
    }

    @Test
    void upsertDetails_NullDetailsList_EarlyReturn() {
        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            service.createAppraisal(validRequest(null));
        }

        verify(dtlRepository, never()).save(any());
    }

    @Test
    void upsertDetails_EmptyDetailsList_EarlyReturn() {
        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            service.createAppraisal(validRequest(new ArrayList<>()));
        }

        verify(dtlRepository, never()).save(any());
    }

    @Test
    void upsertDetails_NoChangeAction_Skipped() {
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        dtlReq.setActionType(ActionType.noChange);

        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(self.getAppraisalById(1L)).thenReturn(Map.of());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            service.createAppraisal(validRequest(List.of(dtlReq)));
        }

        verify(dtlRepository, never()).save(any());
    }

    @Test
    void upsertDetails_IsCreated_AutoAssigned_InCreateMode() {
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        // actionType null + createMode=true → auto-assigned isCreated
        dtlReq.setEmployeePoid(100L);

        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(dtlRepository.findMaxDetRowIdByTransactionPoid(1L)).thenReturn(0L);
        when(dtlRepository.save(any())).thenReturn(mockDtl);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(100L))).thenReturn("Y");

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            service.createAppraisal(validRequest(List.of(dtlReq)));
        }

        verify(dtlRepository).save(any());
    }

    @Test
    void upsertDetails_IsCreated_Explicit_InUpdateMode() {
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        dtlReq.setActionType(ActionType.isCreated);
        dtlReq.setEmployeePoid(100L);

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(dtlRepository.findMaxDetRowIdByTransactionPoid(1L)).thenReturn(0L);
        when(dtlRepository.save(any())).thenReturn(mockDtl);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(100L))).thenReturn("Y");

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            service.updateAppraisal(1L, validRequest(List.of(dtlReq)));
        }

        verify(dtlRepository).save(any());
    }

    @Test
    void upsertDetails_IsDeleted_Success() {
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        dtlReq.setActionType(ActionType.isDeleted);
        dtlReq.setDetRowId(1L);

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(dtlRepository.findById(any())).thenReturn(Optional.of(mockDtl));
        when(self.getAppraisalById(1L)).thenReturn(Map.of());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            service.updateAppraisal(1L, validRequest(List.of(dtlReq)));
        }

        verify(dtlRepository).deleteById(any());
        verify(loggingService).logDelete(any(), any(), any());
    }

    @Test
    void upsertDetails_IsDeleted_DetailNotFound_ThrowsException() {
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        dtlReq.setActionType(ActionType.isDeleted);
        dtlReq.setDetRowId(99L);

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(dtlRepository.findById(any())).thenReturn(Optional.empty());

        HrAppraisalRequest request = validRequest(List.of(dtlReq));
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertThrows(ResourceNotFoundException.class,
                    () -> service.updateAppraisal(1L, request));
        }
    }

    @Test
    void upsertDetails_IsUpdated_Success() {
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        dtlReq.setActionType(ActionType.isUpdated);
        dtlReq.setDetRowId(1L);
        dtlReq.setEmployeePoid(100L);

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(dtlRepository.findById(any())).thenReturn(Optional.of(mockDtl));
        when(dtlRepository.save(any())).thenReturn(mockDtl);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            service.updateAppraisal(1L, validRequest(List.of(dtlReq)));
        }

        verify(dtlRepository).save(any());
        verify(loggingService).createLog(any(), any(), any(), any(), any(), any());
    }

    @Test
    void upsertDetails_IsUpdated_DetailNotFound_ThrowsException() {
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        dtlReq.setActionType(ActionType.isUpdated);
        dtlReq.setDetRowId(99L);

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(dtlRepository.findById(any())).thenReturn(Optional.empty());

        HrAppraisalRequest request = validRequest(List.of(dtlReq));
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertThrows(ResourceNotFoundException.class,
                    () -> service.updateAppraisal(1L, request));
        }
    }

    @Test
    void upsertDetails_HdrNotFound_ThrowsException() {
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        dtlReq.setActionType(ActionType.isCreated);

        // first findById (updateAppraisal) succeeds, second (upsertDetails) fails
        when(hdrRepository.findById(1L))
                .thenReturn(Optional.of(mockHdr))
                .thenReturn(Optional.empty());
        when(hdrRepository.save(any())).thenReturn(mockHdr);

        HrAppraisalRequest request = validRequest(List.of(dtlReq));
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertThrows(ResourceNotFoundException.class,
                    () -> service.updateAppraisal(1L, request));
        }
    }

    // ── end upsertDetails ─────────────────────────────────────────────────

    @Test
    void deleteAppraisal_Success() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        service.deleteAppraisal(1L, new DeleteReasonDto());

        verify(documentDeleteService).deleteDocument(any(), any(), any(), any(), any());
    }

    @Test
    void deleteAppraisal_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());
        DeleteReasonDto reason = new DeleteReasonDto();

        assertThrows(ResourceNotFoundException.class, () -> service.deleteAppraisal(1L, reason));
    }

    @Test
    void recalculateDetail_AmountEdit_Success() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("AMOUNT_EDIT");
        request.setPeriodFrom(LocalDate.of(2024, 1, 1));
        
        HrAppraisalDtlRequest dtlRequest = new HrAppraisalDtlRequest();
        dtlRequest.setCurBasicSalary(BigDecimal.valueOf(5000));
        dtlRequest.setNewBasicSalary(BigDecimal.valueOf(5500));
        request.setDetail(dtlRequest);

        Map<String, Object> result = service.recalculateDetail(request);

        assertNotNull(result);
        assertEquals("AMOUNT_EDIT", result.get("mode"));
        assertNotNull(result.get("detail"));
    }

    @Test
    void recalculateDetail_PercentEdit_Success() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("PERCENT_EDIT");
        request.setFieldName("NewIncrementPer");
        request.setPeriodFrom(LocalDate.of(2024, 1, 1));
        
        HrAppraisalDtlRequest dtlRequest = new HrAppraisalDtlRequest();
        dtlRequest.setCurBasicSalary(BigDecimal.valueOf(5000));
        dtlRequest.setNewIncrementPer(BigDecimal.valueOf(10));
        request.setDetail(dtlRequest);

        Map<String, Object> result = service.recalculateDetail(request);

        assertNotNull(result);
        assertEquals("PERCENT_EDIT", result.get("mode"));
    }

    @Test
    void recalculateDetail_NetDiffEdit_Success() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("NET_DIFF_EDIT");
        request.setPeriodFrom(LocalDate.of(2024, 1, 1));
        request.setNetDiffAmount(BigDecimal.valueOf(500));
        request.setAppraisalBasicPercent(BigDecimal.valueOf(60));
        request.setAppraisalFixedPercent(BigDecimal.valueOf(40));

        HrAppraisalDtlRequest dtlRequest = new HrAppraisalDtlRequest();
        dtlRequest.setCurBasicSalary(BigDecimal.valueOf(5000));
        dtlRequest.setCurFaAlw(BigDecimal.valueOf(2000));
        request.setDetail(dtlRequest);

        Map<String, Object> result = service.recalculateDetail(request);

        assertNotNull(result);
        assertEquals("NET_DIFF_EDIT", result.get("mode"));
    }

    // ── recalculateDetail new branches ───────────────────────────────────

    @Test
    void recalculateDetail_NetDiffEdit_NullNetDiffAmount_ThrowsException() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("NET_DIFF_EDIT");
        request.setAppraisalBasicPercent(BigDecimal.valueOf(60));
        request.setAppraisalFixedPercent(BigDecimal.valueOf(40));
        request.setDetail(new HrAppraisalDtlRequest());
        assertThrows(ValidationException.class, () -> service.recalculateDetail(request));
    }

    @Test
    void recalculateDetail_NetDiffEdit_ZeroNetDiffAmount_NoRecalculation() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("NET_DIFF_EDIT");
        request.setNetDiffAmount(BigDecimal.ZERO);
        request.setAppraisalBasicPercent(BigDecimal.valueOf(60));
        request.setAppraisalFixedPercent(BigDecimal.valueOf(40));
        request.setDetail(new HrAppraisalDtlRequest());
        assertDoesNotThrow(() -> service.recalculateDetail(request));
    }

    // line 229/232/237: null-check false branches — basicPercent/fixedPercent not null but sum != 100
    @Test
    void recalculateDetail_NetDiffEdit_NullBasicPercent_ThrowsException() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("NET_DIFF_EDIT");
        request.setNetDiffAmount(BigDecimal.valueOf(500));
        request.setAppraisalFixedPercent(BigDecimal.valueOf(40));
        request.setDetail(new HrAppraisalDtlRequest());
        assertThrows(ValidationException.class, () -> service.recalculateDetail(request));
    }

    // line 232: basicPercent not null — passes through to fixedPercent null check
    @Test
    void recalculateDetail_NetDiffEdit_NullFixedPercent_ThrowsException() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("NET_DIFF_EDIT");
        request.setNetDiffAmount(BigDecimal.valueOf(500));
        request.setAppraisalBasicPercent(BigDecimal.valueOf(60));
        request.setDetail(new HrAppraisalDtlRequest());
        assertThrows(ValidationException.class, () -> service.recalculateDetail(request));
    }

    // line 237: fixedPercent not null — passes through to percent sum check
    @Test
    void recalculateDetail_NetDiffEdit_InvalidPercentSum_ThrowsException() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("NET_DIFF_EDIT");
        request.setNetDiffAmount(BigDecimal.valueOf(500));
        request.setAppraisalBasicPercent(BigDecimal.valueOf(50));
        request.setAppraisalFixedPercent(BigDecimal.valueOf(40)); // 50+40 != 100
        request.setDetail(new HrAppraisalDtlRequest());
        assertThrows(ValidationException.class, () -> service.recalculateDetail(request));
    }

    @Test
    void recalculateDetail_NullDetail_ThrowsException() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("AMOUNT_EDIT");
        // detail is null
        assertThrows(ValidationException.class, () -> service.recalculateDetail(request));
    }

    @Test
    void recalculateDetail_NullRequest_ThrowsException() {
        assertThrows(ValidationException.class, () -> service.recalculateDetail(null));
    }

    @Test
    void recalculateDetail_NullMode_ThrowsException() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setDetail(new HrAppraisalDtlRequest());
        assertThrows(ValidationException.class, () -> service.recalculateDetail(request));
    }

    @Test
    void recalculateDetail_BlankMode_ThrowsException() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setDetail(new HrAppraisalDtlRequest());
        request.setMode("   ");
        assertThrows(ValidationException.class, () -> service.recalculateDetail(request));
    }

    @Test
    void recalculateDetail_UnsupportedMode_ThrowsException() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("INVALID_MODE");
        request.setDetail(new HrAppraisalDtlRequest());

        assertThrows(ValidationException.class, () -> service.recalculateDetail(request));
    }

    @Test
    void batchUpdateSp_BothPercentagesZero_ThrowsException() {
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setBasicIncrementPercent(BigDecimal.ZERO);
        request.setBonusPercent(BigDecimal.ZERO);

        assertThrows(ValidationException.class, () -> service.batchUpdateSp(1L, request));
    }

    @Test
    void batchUpdateSp_NonZeroBasicZeroBonus_Success() {
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setBasicIncrementPercent(BigDecimal.valueOf(5));
        request.setBonusPercent(BigDecimal.ZERO);
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            assertDoesNotThrow(() -> service.batchUpdateSp(1L, request));
            assertFalse(sp.constructed().isEmpty());
        }
    }

    @Test
    void batchUpdateSp_ZeroBasicNonZeroBonus_Success() {
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setBasicIncrementPercent(BigDecimal.ZERO);
        request.setBonusPercent(BigDecimal.valueOf(15));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            assertDoesNotThrow(() -> service.batchUpdateSp(1L, request));
            assertFalse(sp.constructed().isEmpty());
        }
    }

    @Test
    void loadAppraisalDataSp_NullActionType_ThrowsException() {
        assertThrows(ValidationException.class, () -> service.loadAppraisalDataSp(1L, null));
    }

    @Test
    void loadAppraisalDataSp_BlankActionType_ThrowsException() {
        assertThrows(ValidationException.class, () -> service.loadAppraisalDataSp(1L, ""));
    }

    @Test
    void arrearsSp_NullPayrollPoid_ThrowsException() {
        assertThrows(ValidationException.class, () -> service.arrearsSp(1L, null));
    }

    @Test
    void updateMasterSp_NoApprovedBy_ThrowsException() {
        mockHdr.setApprovedBy(null);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        HrAppraisalActionRequest request = new HrAppraisalActionRequest();

        assertThrows(ValidationException.class, () -> service.updateMasterSp(1L, request));
    }

    @Test
    void updateMasterSp_BlankApprovedBy_ThrowsException() {
        mockHdr.setApprovedBy("");
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        HrAppraisalActionRequest request = new HrAppraisalActionRequest();

        assertThrows(ValidationException.class, () -> service.updateMasterSp(1L, request));
    }

    // line 281: salary exists — employee passes filter, list stays empty, no throw
    @Test
    void updateMasterSp_EmployeeHasSalary_Success() {
        mockDtl.setNetIncrement(BigDecimal.valueOf(500));
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        when(salaryMasterRepository.existsByEmployeePoid(100L)).thenReturn(true);
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setBasicIncrementPercent(BigDecimal.valueOf(5));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            assertDoesNotThrow(() -> service.updateMasterSp(1L, request));
        }
    }

    @Test
    void updateMasterSp_EmployeeWithoutSalary_ThrowsException() {
        mockDtl.setNetIncrement(BigDecimal.valueOf(500));
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        when(salaryMasterRepository.existsByEmployeePoid(100L)).thenReturn(false);
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        assertThrows(ValidationException.class, () -> service.updateMasterSp(1L, request));
    }

    // line 279: netIncrement != null && compareTo != 0 — false branch: netIncrement is zero
    @Test
    void updateMasterSp_ZeroNetIncrement_NoSalaryCheck_Success() {
        mockDtl.setNetIncrement(BigDecimal.ZERO);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setBasicIncrementPercent(BigDecimal.valueOf(5));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            assertDoesNotThrow(() -> service.updateMasterSp(1L, request));
        }
    }

    @Test
    void updateMasterSp_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        assertThrows(ResourceNotFoundException.class, () -> service.updateMasterSp(1L, request));
    }

    @Test
    void createJvSp_NoBonusAmounts_ThrowsException() {
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        assertThrows(ValidationException.class, () -> service.createJvSp(1L));
    }

    // line 298: newBonus != null but == 0 — anyMatch returns false
    @Test
    void createJvSp_ZeroBonus_ThrowsException() {
        mockDtl.setNewBonus(BigDecimal.ZERO);
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        assertThrows(ValidationException.class, () -> service.createJvSp(1L));
    }

    @Test
    void bankFileSp_NoBonusEmployees_ThrowsException() {
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        assertThrows(ValidationException.class, () -> service.bankFileSp(1L));
    }

    // line 323: newBonus != null but == 0
    @Test
    void bankFileSp_ZeroBonus_ThrowsException() {
        mockDtl.setNewBonus(BigDecimal.ZERO);
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        assertThrows(ValidationException.class, () -> service.bankFileSp(1L));
    }

    // line 341: arrears != null but == 0 — arrearsCount stays 0, no throw
    @Test
    void arrearsSp_ZeroArrears_MaxRowIdPositive_Success() {
        mockDtl.setArrears(BigDecimal.ZERO);
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        when(payrollVarDtlRepository.findMaxDetRowIdByTransactionPoid(200L)).thenReturn(1L);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            assertDoesNotThrow(() -> service.arrearsSp(1L, 200L));
        }
    }

    @Test
    void arrearsSp_AlreadyHasEntries_ThrowsException() {
        HrAppraisalDtl dtlWithArrears = new HrAppraisalDtl();
        dtlWithArrears.setArrears(BigDecimal.valueOf(1000));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(dtlWithArrears));
        when(payrollVarDtlRepository.findMaxDetRowIdByTransactionPoid(200L)).thenReturn(1L);
        assertThrows(ValidationException.class, () -> service.arrearsSp(1L, 200L));
    }

    @Test
    void arrearsCalcSp_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.arrearsCalcSp(1L));
    }

    @Test
    void arrearsCalcSp_NullPeriodFrom_ThrowsException() {
        mockHdr.setPeriodFrom(null);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        assertThrows(ValidationException.class, () -> service.arrearsCalcSp(1L));
    }

    @Test
    void arrearsCalcSp_NoEligibleEmployees_ThrowsException() {
        mockHdr.setPeriodFrom(LocalDate.of(2024, 1, 1));
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        assertThrows(ValidationException.class, () -> service.arrearsCalcSp(1L));
    }

    // line 364: netIncrement != null but == 0 — anyMatch returns false
    @Test
    void arrearsCalcSp_ZeroNetIncrement_ThrowsException() {
        mockHdr.setPeriodFrom(LocalDate.of(2024, 1, 1));
        mockDtl.setNetIncrement(BigDecimal.ZERO);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        assertThrows(ValidationException.class, () -> service.arrearsCalcSp(1L));
    }

    @Test
    void upsertDetails_IsCreated_DuplicateEmployee_ThrowsException() {
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        dtlReq.setActionType(ActionType.isCreated);
        dtlReq.setEmployeePoid(100L);

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(dtlRepository.findByTransactionPoidAndEmployeePoid(1L, 100L)).thenReturn(List.of(mockDtl));

        HrAppraisalRequest request = validRequest(List.of(dtlReq));
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            assertThrows(ValidationException.class, () -> service.updateAppraisal(1L, request));
        }
    }

    // line 467: employeePoid == null — skip duplicate check, go straight to save
    @Test
    void upsertDetails_IsCreated_NullEmployeePoid_NoCheck() {
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        dtlReq.setActionType(ActionType.isCreated);
        // employeePoid is null — duplicate check skipped

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(dtlRepository.findMaxDetRowIdByTransactionPoid(1L)).thenReturn(0L);
        when(dtlRepository.save(any())).thenReturn(mockDtl);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());

        HrAppraisalRequest request = validRequest(List.of(dtlReq));
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            assertDoesNotThrow(() -> service.updateAppraisal(1L, request));
        }
        verify(dtlRepository).save(any());
    }

    @Test
    void deleteAppraisal_FinancialYearError_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), any(), any()))
                .thenReturn("ERROR: outside financial period");
        DeleteReasonDto reason = new DeleteReasonDto();
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            assertThrows(ValidationException.class, () -> service.deleteAppraisal(1L, reason));
        }
    }

    @Test
    void createAppraisal_FinancialYearError_ThrowsException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setTransactionDate(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), any(), any()))
                .thenReturn("ERROR: outside financial period");
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            assertThrows(ValidationException.class, () -> service.createAppraisal(request));
        }
    }

    @Test
    void updateAppraisal_FinancialYearError_ThrowsException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setTransactionDate(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), any(), any()))
                .thenReturn("ERROR: outside financial period");
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            assertThrows(ValidationException.class, () -> service.updateAppraisal(1L, request));
        }
    }

    @Test
    void updateAppraisal_TransactionDateChanged_YearError_ThrowsException() {
        mockHdr.setTransactionDate(LocalDate.of(2023, 1, 1));
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setTransactionDate(LocalDate.of(2024, 6, 1));
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), any(), any()))
                .thenReturn(null)  // first call: financial year OK
                .thenReturn("ERROR: transaction year invalid"); // second call: transaction year error
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            assertThrows(ValidationException.class, () -> service.updateAppraisal(1L, request));
        }
    }

    // line 612: transactionYearResult not null but no ERROR — no throw
    @Test
    void updateAppraisal_TransactionDateChanged_YearOk_NonNullResult_Success() {
        mockHdr.setTransactionDate(LocalDate.of(2023, 1, 1));
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setTransactionDate(LocalDate.of(2024, 6, 1));
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);
        request.setDetails(new ArrayList<>());
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), any(), any()))
                .thenReturn("OK")   // financial year OK (non-null, no ERROR)
                .thenReturn("OK");  // transaction year OK (non-null, no ERROR)
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            assertDoesNotThrow(() -> service.updateAppraisal(1L, request));
        }
    }

    // line 605: existingDate == transactionDate — second jdbcTemplate call skipped
    @Test
    void updateAppraisal_TransactionDateUnchanged_NoYearCheck() {
        mockHdr.setTransactionDate(LocalDate.of(2024, 6, 1));
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setTransactionDate(LocalDate.of(2024, 6, 1)); // same as existing
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);
        request.setDetails(new ArrayList<>());
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), any(), any())).thenReturn(null);
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            assertDoesNotThrow(() -> service.updateAppraisal(1L, request));
        }
        // only one jdbcTemplate call (financial year), not two
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(String.class), any(), any());
    }

    @Test
    void validateFinancialYear_NullResult_NoException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setTransactionDate(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);
        request.setDetails(new ArrayList<>());
        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), any(), any())).thenReturn(null);
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            assertDoesNotThrow(() -> service.createAppraisal(request));
        }
    }

    // line 595: financialYearResult not null but no ERROR — no throw
    @Test
    void validateFinancialYear_NonNullOkResult_NoException() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test");
        request.setPeriodFrom(LocalDate.now());
        request.setTransactionDate(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);
        request.setDetails(new ArrayList<>());
        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), any(), any())).thenReturn("OK");
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            assertDoesNotThrow(() -> service.createAppraisal(request));
        }
    }

    // ── Point 4: stored procedure happy paths ─────────────────────────────

    private MockedConstruction<SimpleJdbcCall> mockSp() {
        return mockConstruction(SimpleJdbcCall.class, (mock, ctx) -> {
            when(mock.withProcedureName(anyString())).thenReturn(mock);
            when(mock.declareParameters(any(org.springframework.jdbc.core.SqlParameter[].class))).thenReturn(mock);
            when(mock.execute(anyMap())).thenReturn(Map.of("P_STATUS", "SUCCESS"));
        });
    }

    @Test
    void getDetailsSp_Success() throws Exception {
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
        when(mockCallableStatement.getString(5)).thenReturn("SUCCESS");
        when(mockCallableStatement.getObject(4)).thenReturn(null);
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            assertDoesNotThrow(() -> service.getDetailsSp(1L, 100L));
        }
    }

    @Test
    void loadAppraisalDataSp_Success() {
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserId).thenReturn("user1");
            assertDoesNotThrow(() -> service.loadAppraisalDataSp(1L, "LOAD_EMPLOYEES_BLANK_DATA"));
        }
    }

    @Test
    void clearAppraisalDataSp_Success() {
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserId).thenReturn("user1");
            assertDoesNotThrow(() -> service.clearAppraisalDataSp(1L));
        }
    }

    @Test
    void batchUpdateSp_Success() {
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setBasicIncrementPercent(BigDecimal.valueOf(5));
        request.setBonusPercent(BigDecimal.valueOf(10));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            assertDoesNotThrow(() -> service.batchUpdateSp(1L, request));
        }
    }

    @Test
    void updateDataSp_Success() {
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setAdditionalData("extra");
        when(dtlRepository.findByTransactionPoidAndEmployeePoid(1L, 100L)).thenReturn(List.of(mockDtl));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            assertDoesNotThrow(() -> service.updateDataSp(1L, 100L, request));
        }
        // Status must be set to "Updated" before the SP call (matches legacy ADF commit behaviour)
        assertEquals("Updated", mockDtl.getStatus());
        verify(dtlRepository).saveAll(anyList());
    }

    @Test
    void updateDataSp_NullEmployeePoid_ThrowsException() {
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        assertThrows(ValidationException.class, () -> service.updateDataSp(1L, null, request));
    }

    @Test
    void updateDataSp_EmployeeNotInAppraisal_ThrowsException() {
        when(dtlRepository.findByTransactionPoidAndEmployeePoid(1L, 100L)).thenReturn(List.of());
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        assertThrows(ResourceNotFoundException.class, () -> service.updateDataSp(1L, 100L, request));
    }

    @Test
    void updateMasterSp_Success() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setBasicIncrementPercent(BigDecimal.valueOf(5));
        request.setBonusPercent(BigDecimal.valueOf(10));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            // SP sets COMPLETED='Y' on HDR — response must carry updated header
            Map<String, Object> result = service.updateMasterSp(1L, request);
            assertNotNull(result.get("header"));
        }
    }

    @Test
    void createJvSp_Success() {
        HrAppraisalDtl dtlWithBonus = new HrAppraisalDtl();
        dtlWithBonus.setNewBonus(BigDecimal.valueOf(500));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(dtlWithBonus));
        // SP writes JV_DOC_REF/JV_DOC_POID to HDR — re-fetch after clear
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            Map<String, Object> result = service.createJvSp(1L);
            assertNotNull(result.get("header"));
        }
    }

    @Test
    void sendEmailSp_Success() {
        // SP updates LETTER_EMAILED_ON on DTL rows and HDR — service re-fetches both
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            Map<String, Object> result = service.sendEmailSp(1L, "N");
            assertNotNull(result.get("details"));
            assertNotNull(result.get("totals"));
            assertNotNull(result.get("header"));
        }
    }

    @Test
    void bankFileSp_Success() {
        HrAppraisalDtl dtlWithBonus = new HrAppraisalDtl();
        dtlWithBonus.setNewBonus(BigDecimal.valueOf(500));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(dtlWithBonus));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            assertDoesNotThrow(() -> service.bankFileSp(1L));
        }
    }

    @Test
    void arrearsSp_Success() {
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(payrollVarDtlRepository.findMaxDetRowIdByTransactionPoid(200L)).thenReturn(0L);
        // SP stores ARREARS_PAYROLL_POID on HDR — service re-fetches header after SP
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            Map<String, Object> result = service.arrearsSp(1L, 200L);
            assertNotNull(result.get("header"));
        }
    }

    @Test
    void arrearsCalcSp_Success() {
        mockHdr.setPeriodFrom(LocalDate.of(2024, 1, 1));
        HrAppraisalDtl dtlWithIncrement = new HrAppraisalDtl();
        dtlWithIncrement.setNetIncrement(BigDecimal.valueOf(500));
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(dtlWithIncrement));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            Map<String, Object> result = service.arrearsCalcSp(1L);
            // SP updates arrears per employee row — response must include refreshed details + totals
            assertNotNull(result.get("details"));
            assertNotNull(result.get("totals"));
        }
    }

    // ── Point 5+6: print / generatePdf branches ───────────────────────────

    @Test
    void printA3_Success() throws Exception {
        JasperReport report = mock(JasperReport.class);
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load("EmpAppraisal_A3.jrxml")).thenReturn(report);
        when(printService.fillReportToPdf(any(), anyMap(), any())).thenReturn("PDF".getBytes());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            byte[] result = service.printA3(1L);
            assertNotNull(result);
        }
    }

    @Test
    void printByCompany_Success() throws Exception {
        JasperReport report = mock(JasperReport.class);
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load("EmpAppraisal_ByCompany.jrxml")).thenReturn(report);
        when(printService.fillReportToPdf(any(), anyMap(), any())).thenReturn("PDF".getBytes());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            byte[] result = service.printByCompany(1L);
            assertNotNull(result);
        }
    }

    @Test
    void printBank_Success() throws Exception {
        JasperReport report = mock(JasperReport.class);
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load("HrAppraisalBankReportPrint.jrxml")).thenReturn(report);
        when(printService.fillReportToPdf(any(), anyMap(), any())).thenReturn("PDF".getBytes());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            byte[] result = service.printBank(1L);
            assertNotNull(result);
        }
    }

    @Test
    void printLetter_WithEmployeePoid_Success() throws Exception {
        JasperReport report = mock(JasperReport.class);
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load("HrAppraisalLetter.jrxml")).thenReturn(report);
        when(printService.fillReportToPdf(any(), anyMap(), any())).thenReturn("PDF".getBytes());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            // employeePoid != null branch in generatePdf
            byte[] result = service.printLetter(1L, 100L);
            assertNotNull(result);
        }
    }

    @Test
    void printLetter_WithoutEmployeePoid_PrintsAllEmployees() throws Exception {
        JasperReport report = mock(JasperReport.class);
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load("HrAppraisalLetter.jrxml")).thenReturn(report);
        when(printService.fillReportToPdf(any(), anyMap(), any())).thenReturn("PDF".getBytes());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            // null employeePoid → legacy PrintLetterForEmployees (all employees, no EMPLOYEE_POID param)
            byte[] result = service.printLetter(1L, null);
            assertNotNull(result);
        }
    }

    @Test
    void generatePdf_JRException_Rethrown() throws Exception {
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load(anyString())).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), anyMap(), any()))
                .thenThrow(new JRException("fill failed"));

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            assertThrows(JRException.class, () -> service.printA3(1L));
        }
    }

    @Test
    void generatePdf_NonJRException_WrappedAsJRException() throws Exception {
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load(anyString())).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), anyMap(), any()))
                .thenThrow(new RuntimeException("unexpected"));

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");
            JRException ex = assertThrows(JRException.class, () -> service.printA3(1L));
            assertNotNull(ex.getCause());
        }
    }

    // ── Point 8: resolveAction edge cases ─────────────────────────────────

    @Test
    void resolveAction_CreateModeTrue_ExplicitActionType_UsesExplicitType() {
        // createMode=true but actionType explicitly set to isUpdated — should use isUpdated, not override
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        dtlReq.setActionType(ActionType.isUpdated);
        dtlReq.setDetRowId(1L);
        dtlReq.setEmployeePoid(100L);

        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(dtlRepository.findById(any())).thenReturn(Optional.of(mockDtl));
        when(dtlRepository.save(any())).thenReturn(mockDtl);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            service.createAppraisal(validRequest(List.of(dtlReq)));
        }

        // isUpdated path executed — save called (not findMaxDetRowId)
        verify(dtlRepository, never()).findMaxDetRowIdByTransactionPoid(any());
        verify(dtlRepository).save(any());
    }

    // line 341: dtl.getArrears() == null — null branch of arrears filter
    @Test
    void arrearsSp_NullArrears_TreatedAsZeroCount_Success() {
        // mockDtl has arrears=null → filter predicate short-circuits on null check → arrearsCount stays 0
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(mockDtl));
        when(payrollVarDtlRepository.findMaxDetRowIdByTransactionPoid(200L)).thenReturn(0L);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            assertDoesNotThrow(() -> service.arrearsSp(1L, 200L));
        }
    }

    // line 612: nz(null) — covers the null branch returning BigDecimal.ZERO
    @Test
    void batchUpdateSp_NullPercentages_TreatedAsZero_ThrowsException() {
        // both null → nz returns ZERO for each → both zero → ValidationException
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        // basicIncrementPercent and bonusPercent left null
        assertThrows(ValidationException.class, () -> service.batchUpdateSp(1L, request));
    }

    @Test
    void resolveAction_CreateModeFalse_NullActionType_ReturnsNull_Skipped() {
        // createMode=false, actionType=null → resolveAction returns null → row skipped
        HrAppraisalDtlRequest dtlReq = new HrAppraisalDtlRequest();
        // actionType is null, createMode=false

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(self.getAppraisalById(1L)).thenReturn(Map.of());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            service.updateAppraisal(1L, validRequest(List.of(dtlReq)));
        }

        verify(dtlRepository, never()).save(any());
        verify(dtlRepository, never()).deleteById(any());
    }
}
