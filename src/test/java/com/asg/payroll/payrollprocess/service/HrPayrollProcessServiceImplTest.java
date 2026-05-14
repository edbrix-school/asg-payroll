package com.asg.payroll.payrollprocess.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.service.PrintService;
import com.asg.payroll.employeeappraisal.entity.HrPayrollVarAlwdedDtl;
import com.asg.payroll.employeeappraisal.repository.HrPayrollVarAlwdedDtlRepository;
import com.asg.payroll.exceptions.ResourceNotFoundException;
import com.asg.payroll.exceptions.ValidationException;
import com.asg.payroll.payrollprocess.dto.HrPayrollHdrRequest;
import com.asg.payroll.payrollprocess.dto.PayrollActionRequest;
import com.asg.payroll.payrollprocess.entity.HrPayrollHdr;
import com.asg.payroll.payrollprocess.entity.HrPayrollRecurringDtl;
import com.asg.payroll.payrollprocess.repository.*;
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
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrPayrollProcessServiceImplTest {

    @Mock private HrPayrollHdrRepository hdrRepository;
    @Mock private HrPayrollDtlRepository dtlRepository;
    @Mock private HrPayrollProvisionDtlRepository provisionDtlRepository;
    @Mock private HrPayrollRecurringDtlRepository recurringDtlRepository;
    @Mock private HrPayrollVarAlwdedDtlRepository varAlwdedDtlRepository;
    @Mock private DocumentSearchService documentSearchService;
    @Mock private DocumentDeleteService documentDeleteService;
    @Mock private LoggingService loggingService;
    @Mock private LovDataService lovDataService;
    @Mock private PrintService printService;
    @Mock private DataSource dataSource;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private EntityManager entityManager;
    @Mock private HrPayrollProcessService self;

    @InjectMocks
    private HrPayrollProcessServiceImpl service;

    private HrPayrollHdr mockHdr;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "self", self);
        mockHdr = new HrPayrollHdr();
        mockHdr.setTransactionPoid(1L);
        mockHdr.setDocRef("PAY-001");
        mockHdr.setPayrollMonth(LocalDate.of(2024, 1, 31));
        mockHdr.setAttendTranPoid(10L);
    }

    // ── listPayrolls ──────────────────────────────────────────────────────────

    @Test
    void listPayrolls_Success() {
        RawSearchResult raw = new RawSearchResult(List.of(new HashMap<>()), new HashMap<>(), 1L);
        when(documentSearchService.resolveOperator(any())).thenReturn("AND");
        when(documentSearchService.resolveIsDeleted(any())).thenReturn("N");
        when(documentSearchService.resolveDateFilters(any(), any(), any(), any())).thenReturn(List.of());
        when(documentSearchService.search(any(), any(), any(), any(), any(), any(), any())).thenReturn(raw);

        Map<String, Object> result = service.listPayrolls(
                "DOC123", new FilterRequestDto("AND", "N", List.of()), PageRequest.of(0, 10), null, null);

        assertNotNull(result);
        verify(documentSearchService).search(any(), any(), any(), any(), any(), any(), any());
    }

    // ── getPayrollById ────────────────────────────────────────────────────────

    @Test
    void getPayrollById_Success() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(varAlwdedDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(recurringDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(provisionDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());

        Map<String, Object> result = service.getPayrollById(1L);

        assertNotNull(result);
        assertEquals(mockHdr, result.get("header"));
    }

    @Test
    void getPayrollById_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getPayrollById(1L));
    }

    @Test
    void getPayrollById_WithAttendTranPoid_EnrichesLov() {
        mockHdr.setAttendTranPoid(10L);
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(varAlwdedDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(recurringDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(provisionDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(lovDataService.getDetailsByPoidAndLovName(10L, "HR_ATTENDANCE_POID"))
                .thenReturn(mock(LovGetListDto.class));

        Map<String, Object> result = service.getPayrollById(1L);

        assertTrue(result.containsKey("attendancePeriodLov"));
        verify(lovDataService).getDetailsByPoidAndLovName(10L, "HR_ATTENDANCE_POID");
    }

    @Test
    void getPayrollById_WithEmployeePoids_EnrichesEmployeeLov() {
        mockHdr.setAttendTranPoid(null);
        HrPayrollVarAlwdedDtl varDtl = new HrPayrollVarAlwdedDtl();
        varDtl.setEmployeePoid(100L);
        HrPayrollRecurringDtl recurDtl = new HrPayrollRecurringDtl();
        recurDtl.setEmployeePoid(200L);

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(varAlwdedDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(varDtl));
        when(recurringDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(recurDtl));
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(provisionDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(lovDataService.getDetailsByPoidAndLovName(anyLong(), eq("EMPLOYEE_NAME")))
                .thenReturn(mock(LovGetListDto.class));

        Map<String, Object> result = service.getPayrollById(1L);

        assertTrue(result.containsKey("employeeLov"));
        verify(lovDataService, times(2)).getDetailsByPoidAndLovName(anyLong(), eq("EMPLOYEE_NAME"));
    }

    @Test
    void getPayrollById_WithAlwdedPoids_EnrichesAllowanceDeductionLov() {
        mockHdr.setAttendTranPoid(null);
        HrPayrollVarAlwdedDtl varDtl = new HrPayrollVarAlwdedDtl();
        varDtl.setAllowanceDeductionPoid(50L);

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(varAlwdedDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of(varDtl));
        when(recurringDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(dtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(provisionDtlRepository.findByTransactionPoid(1L)).thenReturn(List.of());
        when(lovDataService.getDetailsByPoidAndLovName(50L, "EMP_ALOW_DEDUCTION"))
                .thenReturn(mock(LovGetListDto.class));

        Map<String, Object> result = service.getPayrollById(1L);

        assertTrue(result.containsKey("allowanceDeductionLov"));
        verify(lovDataService).getDetailsByPoidAndLovName(50L, "EMP_ALOW_DEDUCTION");
    }

    // ── createPayroll ─────────────────────────────────────────────────────────

    @Test
    void createPayroll_Success() {
        HrPayrollHdrRequest request = validHdrRequest();

        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);
        when(self.getPayrollById(1L)).thenReturn(Map.of("header", mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            Map<String, Object> result = service.createPayroll(request);

            assertNotNull(result);
            verify(hdrRepository).saveAndFlush(any());
        }
    }

    @Test
    void createPayroll_NullPayrollMonth_ThrowsValidationException() {
        HrPayrollHdrRequest request = new HrPayrollHdrRequest();
        request.setAttendTranPoid(10L);

        assertThrows(ValidationException.class, () -> service.createPayroll(request));
    }

    @Test
    void createPayroll_NonMonthEndDate_ThrowsValidationException() {
        HrPayrollHdrRequest request = new HrPayrollHdrRequest();
        request.setAttendTranPoid(10L);
        request.setPayrollMonth(LocalDate.of(2024, 1, 15));

        assertThrows(ValidationException.class, () -> service.createPayroll(request));
    }

    @Test
    void createPayroll_ValidateProc_ErrorStatus_ThrowsValidationException() {
        HrPayrollHdrRequest request = validHdrRequest();

        try (MockedConstruction<SimpleJdbcCall> sp = mockSpWithStatus("ERROR: duplicate payroll");
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertThrows(ValidationException.class, () -> service.createPayroll(request));
        }
    }

    // ── updatePayroll ─────────────────────────────────────────────────────────

    @Test
    void updatePayroll_Success() {
        HrPayrollHdrRequest request = validHdrRequest();

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(self.getPayrollById(1L)).thenReturn(Map.of("header", mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            Map<String, Object> result = service.updatePayroll(1L, request);

            assertNotNull(result);
            verify(hdrRepository).save(any());
        }
    }

    @Test
    void updatePayroll_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updatePayroll(1L, validHdrRequest()));
    }

    @Test
    void updatePayroll_NonMonthEndDate_ThrowsValidationException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        HrPayrollHdrRequest request = new HrPayrollHdrRequest();
        request.setAttendTranPoid(10L);
        request.setPayrollMonth(LocalDate.of(2024, 6, 15));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertThrows(ValidationException.class, () -> service.updatePayroll(1L, request));
        }
    }

    // ── deletePayroll ─────────────────────────────────────────────────────────

    @Test
    void deletePayroll_Success() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        service.deletePayroll(1L, new DeleteReasonDto());

        verify(documentDeleteService).deleteDocument(any(), any(), any(), any(), any());
    }

    @Test
    void deletePayroll_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deletePayroll(1L, new DeleteReasonDto()));
    }

    // ── processPayroll ────────────────────────────────────────────────────────

    @Test
    void processPayroll_Success() {
        PayrollActionRequest request = new PayrollActionRequest();
        request.setAttendTranPoid(10L);
        request.setPayrollMonth(LocalDate.of(2024, 1, 31));

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertDoesNotThrow(() -> service.processPayroll(1L, request));
        }
    }

    @Test
    void processPayroll_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());
        PayrollActionRequest request = new PayrollActionRequest();

        assertThrows(ResourceNotFoundException.class, () -> service.processPayroll(1L, request));
    }

    @Test
    void processPayroll_NullAttendTranPoid_ThrowsValidationException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        PayrollActionRequest request = new PayrollActionRequest();
        request.setPayrollMonth(LocalDate.of(2024, 1, 31));

        assertThrows(ValidationException.class, () -> service.processPayroll(1L, request));
    }

    @Test
    void processPayroll_NullPayrollMonth_ThrowsValidationException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        PayrollActionRequest request = new PayrollActionRequest();
        request.setAttendTranPoid(10L);

        assertThrows(ValidationException.class, () -> service.processPayroll(1L, request));
    }

    // ── processProvision ──────────────────────────────────────────────────────

    @Test
    void processProvision_Success() {
        PayrollActionRequest request = new PayrollActionRequest();
        request.setAttendTranPoid(10L);
        request.setPayrollMonth(LocalDate.of(2024, 1, 31));

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertDoesNotThrow(() -> service.processProvision(1L, request));
        }
    }

    // ── revertPayroll ─────────────────────────────────────────────────────────

    @Test
    void revertPayroll_Success() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertDoesNotThrow(() -> service.revertPayroll(1L));
        }
    }

    @Test
    void revertPayroll_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.revertPayroll(1L));
    }

    // ── loadVariables ─────────────────────────────────────────────────────────

    @Test
    void loadVariables_Success() {
        PayrollActionRequest request = new PayrollActionRequest();
        request.setPayrollMonth(LocalDate.of(2024, 1, 31));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertDoesNotThrow(() -> service.loadVariables(1L, request));
        }
    }

    @Test
    void loadVariables_NullPayrollMonth_ThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.loadVariables(1L, new PayrollActionRequest()));
    }

    // ── loadLoansAdvances ─────────────────────────────────────────────────────

    @Test
    void loadLoansAdvances_Success() {
        PayrollActionRequest request = new PayrollActionRequest();
        request.setPayrollMonth(LocalDate.of(2024, 1, 31));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertDoesNotThrow(() -> service.loadLoansAdvances(1L, request));
        }
    }

    @Test
    void loadLoansAdvances_NullPayrollMonth_ThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> service.loadLoansAdvances(1L, new PayrollActionRequest()));
    }

    // ── createJv ─────────────────────────────────────────────────────────────

    @Test
    void createJv_Success() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            Map<String, Object> result = service.createJv(1L);

            assertNotNull(result);
            assertTrue(result.containsKey("payrollJvStatus"));
            assertTrue(result.containsKey("provisionJvStatus"));
            assertTrue(result.containsKey("bankDvStatus"));
        }
    }

    @Test
    void createJv_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createJv(1L));
    }

    // ── generateBankFile / hsbcApiTransfer ────────────────────────────────────

    @Test
    void generateBankFile_Success() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertDoesNotThrow(() -> service.generateBankFile(1L));
        }
    }

    @Test
    void hsbcApiTransfer_Success() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertDoesNotThrow(() -> service.hsbcApiTransfer(1L));
        }
    }

    // ── syncHrData ────────────────────────────────────────────────────────────

    @Test
    void syncHrData_Success() {
        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertDoesNotThrow(() -> service.syncHrData());
        }
    }

    // ── sendEmail ─────────────────────────────────────────────────────────────

    @Test
    void sendEmail_Success() {
        PayrollActionRequest request = new PayrollActionRequest();
        request.setScheduleOn(java.time.LocalDateTime.of(2024, 2, 1, 9, 0));

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertDoesNotThrow(() -> service.sendEmail(1L, request));
        }
    }

    @Test
    void sendEmail_NullScheduleOn_ThrowsValidationException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        assertThrows(ValidationException.class, () -> service.sendEmail(1L, new PayrollActionRequest()));
    }

    @Test
    void sendEmail_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.sendEmail(1L, new PayrollActionRequest()));
    }

    // ── printPayslip ──────────────────────────────────────────────────────────

    @Test
    void printPayslip_Success() throws Exception {
        JasperReport report = mock(JasperReport.class);
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load("HR/Payslip_without_email.jrxml")).thenReturn(report);
        when(printService.fillReportToPdf(any(), anyMap(), any())).thenReturn("PDF".getBytes());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            byte[] result = service.printPayslip(1L);

            assertNotNull(result);
            assertArrayEquals("PDF".getBytes(), result);
        }
    }

    @Test
    void printPayslip_JRException_Rethrown() throws Exception {
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load(anyString())).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), anyMap(), any()))
                .thenThrow(new JRException("fill failed"));

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertThrows(JRException.class, () -> service.printPayslip(1L));
        }
    }

    @Test
    void printPayslip_NonJRException_WrappedAsJRException() throws Exception {
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load(anyString())).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), anyMap(), any()))
                .thenThrow(new RuntimeException("unexpected error"));

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            JRException ex = assertThrows(JRException.class, () -> service.printPayslip(1L));
            assertNotNull(ex.getCause());
            assertInstanceOf(RuntimeException.class, ex.getCause());
        }
    }

    // ── printPreview ──────────────────────────────────────────────────────────

    @Test
    void printPreview_Success() throws Exception {
        JasperReport report = mock(JasperReport.class);
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load("HR/Payslip.jrxml")).thenReturn(report);
        when(printService.fillReportToPdf(any(), anyMap(), any())).thenReturn("PREVIEW".getBytes());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            byte[] result = service.printPreview(1L);

            assertNotNull(result);
            assertArrayEquals("PREVIEW".getBytes(), result);
        }
    }

    @Test
    void printPreview_JRException_Rethrown() throws Exception {
        when(printService.buildBaseParams(anyLong(), any())).thenReturn(new HashMap<>());
        when(printService.load(anyString())).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), anyMap(), any()))
                .thenThrow(new JRException("fill failed"));

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertThrows(JRException.class, () -> service.printPreview(1L));
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private HrPayrollHdrRequest validHdrRequest() {
        HrPayrollHdrRequest r = new HrPayrollHdrRequest();
        r.setAttendTranPoid(10L);
        r.setPayrollMonth(LocalDate.of(2024, 1, 31));
        return r;
    }

    private MockedConstruction<SimpleJdbcCall> mockSp() {
        return mockConstruction(SimpleJdbcCall.class, (mock, ctx) -> {
            when(mock.withProcedureName(anyString())).thenReturn(mock);
            when(mock.declareParameters(any(org.springframework.jdbc.core.SqlParameter[].class))).thenReturn(mock);
            when(mock.execute(anyMap())).thenReturn(Map.of("P_STATUS", "SUCCESS"));
        });
    }

    private MockedConstruction<SimpleJdbcCall> mockSpWithStatus(String status) {
        return mockConstruction(SimpleJdbcCall.class, (mock, ctx) -> {
            when(mock.withProcedureName(anyString())).thenReturn(mock);
            when(mock.declareParameters(any(org.springframework.jdbc.core.SqlParameter[].class))).thenReturn(mock);
            when(mock.execute(anyMap())).thenReturn(Map.of("P_STATUS", status));
        });
    }
}
