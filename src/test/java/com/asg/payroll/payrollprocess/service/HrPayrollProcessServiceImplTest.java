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
import com.asg.payroll.payrollprocess.dto.*;
import com.asg.payroll.payrollprocess.entity.HrPayrollHdr;
import com.asg.payroll.payrollprocess.entity.HrPayrollRecurringDtl;
import com.asg.payroll.payrollprocess.repository.*;
import jakarta.persistence.EntityManager;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
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

import org.springframework.jdbc.core.ConnectionCallback;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp()) {
            HrPayrollHdrResponse result = service.getPayrollById(1L);

            assertNotNull(result);
            assertEquals(mockHdr.getTransactionPoid(), result.getTransactionPoid());
            assertTrue(result.getAllowEdit());
        }
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

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp()) {
            HrPayrollHdrResponse result = service.getPayrollById(1L);

            assertNotNull(result.getAttendancePeriodLov());
            verify(lovDataService).getDetailsByPoidAndLovName(10L, "HR_ATTENDANCE_POID");
        }
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

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp()) {
            HrPayrollHdrResponse result = service.getPayrollById(1L);

            assertNotNull(result.getEmployeeLov());
            assertFalse(result.getEmployeeLov().isEmpty());
            verify(lovDataService, times(2)).getDetailsByPoidAndLovName(anyLong(), eq("EMPLOYEE_NAME"));
        }
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

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp()) {
            HrPayrollHdrResponse result = service.getPayrollById(1L);

            assertNotNull(result.getAllowanceDeductionLov());
            assertFalse(result.getAllowanceDeductionLov().isEmpty());
            verify(lovDataService).getDetailsByPoidAndLovName(50L, "EMP_ALOW_DEDUCTION");
        }
    }

    // ── createPayroll ─────────────────────────────────────────────────────────

    @Test
    void createPayroll_Success() {
        HrPayrollHdrRequest request = validHdrRequest();

        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);
        when(self.getPayrollById(1L)).thenReturn(new HrPayrollHdrResponse());
        // Mock for validateEmployeeActiveStatus - returns List<Map<String, Object>>
        when(jdbcTemplate.queryForList(anyString(), any(Object.class), any(Object.class), any(Object.class)))
            .thenReturn(new ArrayList<>());
        // Mock for validateWorkingDays - returns Map<String, Object>
        when(jdbcTemplate.queryForMap(anyString(), any(Object.class))).thenReturn(Map.of(
            "ATTENDANCE_FROM", java.sql.Timestamp.valueOf("2024-01-01 00:00:00"),
            "ATTENDANCE_TO", java.sql.Timestamp.valueOf("2024-01-31 23:59:59")
        ));
        // Mock for validateWorkingDays employee query - returns List<Map<String, Object>>
        when(jdbcTemplate.queryForList(anyString(), any(Object.class))).thenReturn(new ArrayList<>());

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            HrPayrollHdrResponse result = service.createPayroll(request);

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
        when(jdbcTemplate.queryForList(anyString(), any(Object.class), any(Object.class), any(Object.class)))
            .thenReturn(new ArrayList<>());
        when(jdbcTemplate.queryForMap(anyString(), any(Object.class))).thenReturn(Map.of(
            "ATTENDANCE_FROM", java.sql.Timestamp.valueOf("2024-01-01 00:00:00"),
            "ATTENDANCE_TO", java.sql.Timestamp.valueOf("2024-01-31 23:59:59")
        ));
        when(jdbcTemplate.queryForList(anyString(), any(Object.class))).thenReturn(new ArrayList<>());

        try (MockedConstruction<SimpleJdbcCall> sp = mockSpWithStatus("ERROR: duplicate payroll");
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertThrows(ValidationException.class, () -> service.createPayroll(request));
        }
    }

    @Test
    void createPayroll_TerminatedEmployee_ThrowsValidationException() {
        HrPayrollHdrRequest request = validHdrRequestWithEmployees();
        
        // Mock terminated employee query result with all required fields
        List<Map<String, Object>> terminatedEmployees = new ArrayList<>();
        terminatedEmployees.add(Map.of(
            "EMPLOYEE_POID", 100L, 
            "EMPLOYEE_NAME", "John Doe", 
            "DISCONTINUED_DATE", java.sql.Timestamp.valueOf("2024-01-15 00:00:00"),
            "JOIN_DATE", java.sql.Timestamp.valueOf("2023-01-01 00:00:00"),
            "ACTIVE", "Y",
            "DISCONTINUED", "Y"
        ));
        
        when(jdbcTemplate.queryForList(anyString(), any(Object.class), any(Object.class), any(Object.class)))
            .thenReturn(terminatedEmployees);
        when(jdbcTemplate.queryForMap(anyString(), any(Object.class))).thenReturn(Map.of(
            "ATTENDANCE_FROM", java.sql.Timestamp.valueOf("2024-01-01 00:00:00"),
            "ATTENDANCE_TO", java.sql.Timestamp.valueOf("2024-01-31 23:59:59")
        ));
        when(jdbcTemplate.queryForList(anyString(), any(Object.class))).thenReturn(new ArrayList<>());
        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);

        try (MockedConstruction<SimpleJdbcCall> sp = mockSpWithStatus("ERROR: Discontinued Employees");
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            ValidationException ex = assertThrows(ValidationException.class, () -> service.createPayroll(request));
            assertTrue(ex.getMessage().contains("ERROR"));
        }
    }

    @Test
    void createPayroll_RecurringAmountExceedsBalance_ThrowsValidationException() {
        HrPayrollHdrRequest request = validHdrRequestWithRecurringDetails();
        
        // Mock empty terminated employees (no validation error from employee status)
        when(jdbcTemplate.queryForList(anyString(), any(Object.class), any(Object.class), any(Object.class)))
            .thenReturn(new ArrayList<>());
        when(jdbcTemplate.queryForMap(anyString(), any(Object.class))).thenReturn(Map.of(
            "ATTENDANCE_FROM", java.sql.Timestamp.valueOf("2024-01-01 00:00:00"),
            "ATTENDANCE_TO", java.sql.Timestamp.valueOf("2024-01-31 23:59:59")
        ));
        when(jdbcTemplate.queryForList(anyString(), any(Object.class))).thenReturn(new ArrayList<>());
        when(hdrRepository.saveAndFlush(any())).thenReturn(mockHdr);

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getGroupPoid).thenReturn(10L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            ValidationException ex = assertThrows(ValidationException.class, () -> service.createPayroll(request));
            assertTrue(ex.getMessage().contains("Recurring amount"));
            assertTrue(ex.getMessage().contains("cannot exceed balance amount"));
        }
    }

    // ── updatePayroll ─────────────────────────────────────────────────────────

    @Test
    void updatePayroll_Success() {
        HrPayrollHdrRequest request = validHdrRequest();

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        when(hdrRepository.save(any())).thenReturn(mockHdr);
        when(self.getPayrollById(1L)).thenReturn(new HrPayrollHdrResponse());
        when(jdbcTemplate.queryForMap(anyString(), any(Object.class))).thenReturn(Map.of(
            "ATTENDANCE_FROM", java.sql.Timestamp.valueOf("2024-01-01 00:00:00"),
            "ATTENDANCE_TO", java.sql.Timestamp.valueOf("2024-01-31 23:59:59")
        ));
        when(jdbcTemplate.queryForList(anyString(), any(Object.class))).thenReturn(new ArrayList<>());

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            HrPayrollHdrResponse result = service.updatePayroll(1L, request);

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

            assertDoesNotThrow(() -> service.processProvision(1L, request, "N"));
        }
    }

    @Test
    void processProvision_WithPostJvY_Success() {
        PayrollActionRequest request = new PayrollActionRequest();
        request.setAttendTranPoid(10L);
        request.setPayrollMonth(LocalDate.of(2024, 1, 31));

        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(20L);
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            assertDoesNotThrow(() -> service.processProvision(1L, request, "Y"));
        }
    }

    @Test
    void processProvision_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());
        PayrollActionRequest request = new PayrollActionRequest();

        assertThrows(ResourceNotFoundException.class, () -> service.processProvision(1L, request, "N"));
    }

    @Test
    void processProvision_NullAttendTranPoid_ThrowsValidationException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        PayrollActionRequest request = new PayrollActionRequest();
        request.setPayrollMonth(LocalDate.of(2024, 1, 31));

        assertThrows(ValidationException.class, () -> service.processProvision(1L, request, "N"));
    }

    @Test
    void processProvision_NullPayrollMonth_ThrowsValidationException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));
        PayrollActionRequest request = new PayrollActionRequest();
        request.setAttendTranPoid(10L);

        assertThrows(ValidationException.class, () -> service.processProvision(1L, request, "N"));
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
    void loadVariables_ReturnsCursorRows() throws SQLException {
        ResultSet cursor = cursorWith(Map.of("EMPLOYEE_POID", 100L, "AMOUNT", 250L));
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getObject(5)).thenReturn(cursor);
        when(cs.getString(6)).thenReturn("SUCCESS");
        stubConnectionCallback(cs);

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            VariableLoadResponse response = service.loadVariables(1L, 2L, 100L, "2024-01-31");

            assertEquals("SUCCESS", response.getStatus());
            assertEquals(1, response.getVariables().size());
            assertEquals(100L, response.getVariables().get(0).get("EMPLOYEE_POID"));
            assertEquals(250L, response.getVariables().get(0).get("AMOUNT"));
        }
    }

    @Test
    void loadVariables_BindsParametersPositionally() throws SQLException {
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getObject(5)).thenReturn(null);
        stubConnectionCallback(cs);

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            service.loadVariables(1L, 2L, 100L, "2024-01-31");

            verify(cs).setLong(1, 1L);
            verify(cs).setLong(2, 2L);
            verify(cs).setLong(3, 100L);
            verify(cs).setDate(4, java.sql.Date.valueOf(LocalDate.of(2024, 1, 31)));
            verify(cs).registerOutParameter(5, oracle.jdbc.OracleTypes.CURSOR);
            verify(cs).registerOutParameter(6, Types.VARCHAR);
        }
    }

    @Test
    void loadVariables_NullPayrollMonth_BindsNullDate() throws SQLException {
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getObject(5)).thenReturn(null);
        stubConnectionCallback(cs);

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            VariableLoadResponse response = service.loadVariables(1L, 2L, 100L, null);

            verify(cs).setNull(4, Types.DATE);
            assertTrue(response.getVariables().isEmpty());
        }
    }

    // ── loadLoansAdvances ─────────────────────────────────────────────────────

    @Test
    void loadLoansAdvances_ReturnsCursorRows() throws SQLException {
        ResultSet cursor = cursorWith(Map.of("EMPLOYEE_POID", 100L, "REF_NO", "L-1"));
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getObject(5)).thenReturn(cursor);
        stubConnectionCallback(cs);

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            LoansAdvancesResponse response = service.loadLoansAdvances(1L, 2L, 100L, LocalDate.of(2024, 1, 31));

            assertEquals(1, response.getLoansAdvances().size());
            assertEquals("L-1", response.getLoansAdvances().get(0).get("REF_NO"));
        }
    }

    @Test
    void loadLoansAdvances_NullPayrollMonth_BindsNullDate() throws SQLException {
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getObject(5)).thenReturn(null);
        stubConnectionCallback(cs);

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            LoansAdvancesResponse response = service.loadLoansAdvances(1L, 2L, 100L, null);

            verify(cs).setNull(4, Types.DATE);
            assertTrue(response.getLoansAdvances().isEmpty());
        }
    }

    // ── createJv ─────────────────────────────────────────────────────────────

    @Test
    void createJv_Success() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.of(mockHdr));

        try (MockedConstruction<SimpleJdbcCall> sp = mockSp();
             MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            JvCreationResponse result = service.createJv(5L, 1L, "BANK");

            assertNotNull(result);
            assertNotNull(result.getPayrollJvStatus());
            assertNotNull(result.getProvisionJvStatus());
            assertNotNull(result.getBankDvStatus());
        }
    }

    @Test
    void createJv_NotFound_ThrowsException() {
        when(hdrRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createJv(5L, 1L, "BANK"));
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

    private HrPayrollHdrRequest validHdrRequestWithEmployees() {
        HrPayrollHdrRequest r = validHdrRequest();
        
        // Add variable details with employee
        com.asg.payroll.payrollprocess.dto.HrPayrollVarAlwdedDtlRequest varDetail = 
            new com.asg.payroll.payrollprocess.dto.HrPayrollVarAlwdedDtlRequest();
        varDetail.setEmployeePoid(100L);
        varDetail.setAllowanceDeductionPoid(50L);
        varDetail.setAmount(java.math.BigDecimal.valueOf(1000));
        r.setVariableDetails(List.of(varDetail));
        
        return r;
    }

    private HrPayrollHdrRequest validHdrRequestWithRecurringDetails() {
        HrPayrollHdrRequest r = validHdrRequest();
        
        // Add recurring details with amount exceeding balance
        com.asg.payroll.payrollprocess.dto.HrPayrollRecurringDtlRequest recurDetail = 
            new com.asg.payroll.payrollprocess.dto.HrPayrollRecurringDtlRequest();
        recurDetail.setEmployeePoid(100L);
        recurDetail.setBalAmt(java.math.BigDecimal.valueOf(500));
        recurDetail.setRecurAmount(java.math.BigDecimal.valueOf(1000)); // Exceeds balance
        r.setRecurringDetails(List.of(recurDetail));
        
        return r;
    }

    private MockedConstruction<SimpleJdbcCall> mockSp() {
        return mockConstruction(SimpleJdbcCall.class, (mock, ctx) -> {
            when(mock.withProcedureName(anyString())).thenReturn(mock);
            when(mock.declareParameters(any(org.springframework.jdbc.core.SqlParameter[].class))).thenReturn(mock);
            when(mock.execute(anyMap())).thenReturn(Map.of("P_STATUS", "SUCCESS"));
        });
    }

    /** Runs the service's ConnectionCallback against a connection that hands back the given statement. */
    @SuppressWarnings("unchecked")
    private void stubConnectionCallback(CallableStatement cs) throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.prepareCall(anyString())).thenReturn(cs);
        when(jdbcTemplate.execute(any(ConnectionCallback.class)))
                .thenAnswer(inv -> ((ConnectionCallback<Object>) inv.getArgument(0)).doInConnection(connection));
    }

    /** A single-row REF CURSOR as ColumnMapRowMapper reads it. */
    private ResultSet cursorWith(Map<String, Object> row) throws SQLException {
        List<String> columns = new ArrayList<>(row.keySet());

        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(columns.size());
        ResultSet rs = mock(ResultSet.class);
        when(rs.getMetaData()).thenReturn(metaData);
        when(rs.next()).thenReturn(true, false);
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            when(metaData.getColumnLabel(i + 1)).thenReturn(column);
            when(metaData.getColumnName(i + 1)).thenReturn(column);
            when(rs.getObject(i + 1)).thenReturn(row.get(column));
        }
        return rs;
    }

    private MockedConstruction<SimpleJdbcCall> mockSpWithStatus(String status) {
        return mockConstruction(SimpleJdbcCall.class, (mock, ctx) -> {
            when(mock.withProcedureName(anyString())).thenReturn(mock);
            when(mock.declareParameters(any(org.springframework.jdbc.core.SqlParameter[].class))).thenReturn(mock);
            when(mock.execute(anyMap())).thenReturn(Map.of("P_STATUS", status));
        });
    }
}
