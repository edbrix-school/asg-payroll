package com.asg.payroll.payrollvariables.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.payroll.payrollvariables.dto.PayrollVariablesRequestDTO;
import com.asg.payroll.payrollvariables.dto.PayrollVariablesResponseDTO;
import com.asg.payroll.payrollvariables.entity.HrPayrollVariablesHdr;
import com.asg.payroll.payrollvariables.repository.PayrollVariablesRepository;
import com.asg.payroll.payrollvariables.service.impl.PayrollVariablesServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollVariablesServiceImplTest {

    @Mock
    private PayrollVariablesRepository repository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private LovDataService lovDataService;

    @Mock
    private LoggingService loggingService;

    @Mock
    private DocumentSearchService documentSearchService;

    @Mock
    private DocumentDeleteService documentDeleteService;

    @InjectMocks
    private PayrollVariablesServiceImpl service;

    private PayrollVariablesRequestDTO requestDTO;
    private HrPayrollVariablesHdr entity;
    private LovGetListDto empDet;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
        requestDTO = PayrollVariablesRequestDTO.builder()
                .transactionDate(LocalDate.now())
                .employeePoid(10L)
                .amount(BigDecimal.valueOf(500))
                .payrollMonth(LocalDate.of(2024, 1, 1))
                .remarks("Test remark")
                .build();

        entity = HrPayrollVariablesHdr.builder()
                .transactionPoid(1L)
                .companyPoid(1L)
                .groupPoid(1L)
                .transactionDate(LocalDate.now())
                .docRef("VAR-001")
                .employeePoid(10L)
                .amount(BigDecimal.valueOf(500))
                .payrollMonth(LocalDate.of(2024, 1, 1))
                .remarks("Test remark")
                .deleted("N")
                .build();

        empDet = new LovGetListDto(10L, "EMP001", "John Doe", 10L, "John Doe", null, null);
    }

    @Test
    void create_Success() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(1L);
            ctx.when(UserContext::getGroupPoid).thenReturn(1L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            when(repository.saveAndFlush(any(HrPayrollVariablesHdr.class))).thenReturn(entity);
            doNothing().when(entityManager).refresh(entity);
            when(lovDataService.getDetailsByPoidAndLovNameFast(10L, "EMPLOYEE_NAME")).thenReturn(empDet);

            PayrollVariablesResponseDTO result = service.create(requestDTO);

            assertNotNull(result);
            assertEquals(1L, result.getTransactionPoid());
            assertEquals(10L, result.getEmployeePoid());
            assertEquals("VAR-001", result.getDocRef());
            assertNotNull(result.getEmployeeDet());
            assertEquals("EMP001", result.getEmployeeDet().getCode());
            verify(entityManager).refresh(entity);
            verify(loggingService).createLogSummaryEntry(eq("DOC123"), eq("1"), anyString());
        }
    }

    @Test
    void create_WithoutEmployee() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(1L);
            ctx.when(UserContext::getGroupPoid).thenReturn(1L);
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            entity.setEmployeePoid(null);
            when(repository.saveAndFlush(any(HrPayrollVariablesHdr.class))).thenReturn(entity);
            doNothing().when(entityManager).refresh(entity);

            PayrollVariablesResponseDTO result = service.create(requestDTO);

            assertNotNull(result);
            assertNull(result.getEmployeeDet());
            verify(lovDataService, never()).getDetailsByPoidAndLovNameFast(any(), any());
            verify(entityManager).refresh(entity);
        }
    }

    @Test
    void create_TriggerError_ORA20001() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(1L);
            ctx.when(UserContext::getGroupPoid).thenReturn(1L);

            when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("ORA-20001: Financial period error"));

            ValidationException ex = assertThrows(ValidationException.class, () -> service.create(requestDTO));
            assertEquals("Changes allowed only within current Financial Period", ex.getMessage());
        }
    }

    @Test
    void create_TriggerError_ORA20002() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(1L);
            ctx.when(UserContext::getGroupPoid).thenReturn(1L);

            when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("ORA-20002: Transaction date error"));

            ValidationException ex = assertThrows(ValidationException.class, () -> service.create(requestDTO));
            assertEquals("Transaction date cannot be updated", ex.getMessage());
        }
    }

    @Test
    void create_GenericDatabaseError() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(1L);
            ctx.when(UserContext::getGroupPoid).thenReturn(1L);

            when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("Constraint violation"));

            ValidationException ex = assertThrows(ValidationException.class, () -> service.create(requestDTO));
            assertTrue(ex.getMessage().contains("Database validation failed"));
        }
    }

    @Test
    void update_Success() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            when(repository.findByTransactionPoid(1L)).thenReturn(Optional.of(entity));
            when(repository.save(any(HrPayrollVariablesHdr.class))).thenReturn(entity);
            when(lovDataService.getDetailsByPoidAndLovNameFast(10L, "EMPLOYEE_NAME")).thenReturn(empDet);

            PayrollVariablesResponseDTO result = service.update(1L, requestDTO);

            assertNotNull(result);
            verify(loggingService).logChanges(any(), any(), any(), eq("DOC123"), eq("1"), any(LogDetailsEnum.class), eq("TRANSACTION_POID"));
        }
    }

    @Test
    void update_NotFound() {
        when(repository.findByTransactionPoid(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, requestDTO));
    }

    @Test
    void update_DatabaseException() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            when(repository.findByTransactionPoid(1L)).thenReturn(Optional.of(entity));
            when(repository.save(any())).thenThrow(new DataIntegrityViolationException("Constraint violation"));

            ValidationException ex = assertThrows(ValidationException.class, () -> service.update(1L, requestDTO));
            assertTrue(ex.getMessage().contains("Database validation failed"));
        }
    }

    @Test
    void getById_Success() {
        when(repository.findByTransactionPoid(1L)).thenReturn(Optional.of(entity));
        when(lovDataService.getDetailsByPoidAndLovNameFast(10L, "EMPLOYEE_NAME")).thenReturn(empDet);

        PayrollVariablesResponseDTO result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTransactionPoid());
        assertNotNull(result.getEmployeeDet());
        assertEquals("John Doe", result.getEmployeeDet().getLabel());
    }

    @Test
    void getById_NotFound() {
        when(repository.findByTransactionPoid(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void getById_NullEmployeePoid() {
        entity.setEmployeePoid(null);
        when(repository.findByTransactionPoid(1L)).thenReturn(Optional.of(entity));

        PayrollVariablesResponseDTO result = service.getById(1L);

        assertNotNull(result);
        assertNull(result.getEmployeeDet());
        verify(lovDataService, never()).getDetailsByPoidAndLovNameFast(any(), any());
    }

    @Test
    void softDelete_Success() {
        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();
        deleteReasonDto.setDeleteReason("Test deletion");

        when(repository.findByTransactionPoid(1L)).thenReturn(Optional.of(entity));
        when(documentDeleteService.deleteDocument(anyLong(), anyString(), anyString(), any(), any())).thenReturn("SUCCESS");

        service.softDelete(1L, deleteReasonDto);

        verify(documentDeleteService).deleteDocument(eq(1L), eq("HR_PAYROLL_VARIABLES_HDR"), eq("TRANSACTION_POID"), eq(deleteReasonDto), eq(entity.getTransactionDate()));
    }

    @Test
    void softDelete_NotFound() {
        when(repository.findByTransactionPoid(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.softDelete(99L, null));
    }

    @Test
    void softDelete_WithNullReason() {
        when(repository.findByTransactionPoid(1L)).thenReturn(Optional.of(entity));
        when(documentDeleteService.deleteDocument(anyLong(), anyString(), anyString(), isNull(), any())).thenReturn("SUCCESS");

        service.softDelete(1L, null);

        verify(documentDeleteService).deleteDocument(eq(1L), eq("HR_PAYROLL_VARIABLES_HDR"), eq("TRANSACTION_POID"), isNull(), any());
    }

    @Test
    void list_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        RawSearchResult rawResult = new RawSearchResult(List.of(Map.of("transactionPoid", "1")), Map.of("transactionPoid", "Transaction ID"), 1L);

        when(documentSearchService.resolveOperator(any())).thenReturn("AND");
        when(documentSearchService.resolveIsDeleted(any())).thenReturn("N");
        when(documentSearchService.resolveDateFilters(any(), eq("TRANSACTION_DATE"), eq(from), eq(to))).thenReturn(new ArrayList<>());
        when(documentSearchService.search(anyString(), anyList(), anyString(), any(Pageable.class), anyString(), anyString(), anyString())).thenReturn(rawResult);

        Map<String, Object> result = service.list("DOC123", null, pageable, from, to);

        assertNotNull(result);
        verify(documentSearchService).search(eq("DOC123"), anyList(), eq("AND"), eq(pageable), eq("N"), eq("DOC_REF"), eq("TRANSACTION_POID"));
    }

    @Test
    void list_WithoutDateRange() {
        Pageable pageable = PageRequest.of(0, 10);
        RawSearchResult rawResult = new RawSearchResult(new ArrayList<>(), new HashMap<>(), 0L);

        when(documentSearchService.resolveOperator(any())).thenReturn("AND");
        when(documentSearchService.resolveIsDeleted(any())).thenReturn("N");
        when(documentSearchService.resolveDateFilters(any(), eq("TRANSACTION_DATE"), isNull(), isNull())).thenReturn(new ArrayList<>());
        when(documentSearchService.search(anyString(), anyList(), anyString(), any(Pageable.class), anyString(), anyString(), anyString())).thenReturn(rawResult);

        Map<String, Object> result = service.list("DOC123", null, pageable, null, null);

        assertNotNull(result);
    }

    @Test
    void list_InvalidDateRange_FromAfterTo() {
        Pageable pageable = PageRequest.of(0, 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.list("DOC123", null, pageable, LocalDate.of(2024, 12, 31), LocalDate.of(2024, 1, 1)));

        assertEquals("Period From must not be after Period To", ex.getMessage());
    }

    @Test
    void list_InvalidDateRange_OnlyFromProvided() {
        Pageable pageable = PageRequest.of(0, 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.list("DOC123", null, pageable, LocalDate.of(2024, 1, 1), null));

        assertEquals("Both periodFrom and periodTo must be specified or both must be empty.", ex.getMessage());
    }

    @Test
    void list_InvalidDateRange_OnlyToProvided() {
        Pageable pageable = PageRequest.of(0, 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.list("DOC123", null, pageable, null, LocalDate.of(2024, 12, 31)));

        assertEquals("Both periodFrom and periodTo must be specified or both must be empty.", ex.getMessage());
    }

    @Test
    void list_WithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        FilterRequestDto filters = new FilterRequestDto("AND", "N", Collections.emptyList());
        RawSearchResult rawResult = new RawSearchResult(new ArrayList<>(), new HashMap<>(), 0L);

        when(documentSearchService.resolveOperator(filters)).thenReturn("AND");
        when(documentSearchService.resolveIsDeleted(filters)).thenReturn("N");
        when(documentSearchService.resolveDateFilters(eq(filters), eq("TRANSACTION_DATE"), isNull(), isNull())).thenReturn(new ArrayList<>());
        when(documentSearchService.search(anyString(), anyList(), anyString(), any(Pageable.class), anyString(), anyString(), anyString())).thenReturn(rawResult);

        Map<String, Object> result = service.list("DOC123", filters, pageable, null, null);

        assertNotNull(result);
    }

    @Test
    void create_NullRequest() {
        assertThrows(ValidationException.class, () -> service.create(null));
    }

    @Test
    void create_DatabaseError_NullMessage() {
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getCompanyPoid).thenReturn(1L);
            ctx.when(UserContext::getGroupPoid).thenReturn(1L);

            when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException(null));

            ValidationException ex = assertThrows(ValidationException.class, () -> service.create(requestDTO));
            assertEquals("Database validation failed: Unknown error", ex.getMessage());
        }
    }
}
