package com.asg.payroll.loansadvances.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.exceptions.ValidationException;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductRequest;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductResponse;
import com.asg.payroll.loansadvances.entity.HrRecurringPayDeduct;
import com.asg.payroll.loansadvances.repository.HrRecurringPayDeductRepository;
import com.asg.payroll.loansadvances.repository.LoansAdvancesProcRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoansAdvancesServiceImplTest {

    @Mock
    private HrRecurringPayDeductRepository repository;

    @Mock
    private LoansAdvancesProcRepository loansAdvancesProcRepository;

    @Mock
    private LoggingService loggingService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private DocumentDeleteService documentDeleteService;

    @Mock
    private DocumentSearchService documentSearchService;

    @InjectMocks
    private LoansAdvancesServiceImpl service;

    private MockedStatic<UserContext> userContextMockedStatic;

    @BeforeEach
    void setUp() {
        userContextMockedStatic = mockStatic(UserContext.class);
        userContextMockedStatic.when(UserContext::getGroupPoid).thenReturn(1L);
        userContextMockedStatic.when(UserContext::getDocumentId).thenReturn("DOC-123");
    }

    @AfterEach
    void tearDown() {
        if (userContextMockedStatic != null) {
            userContextMockedStatic.close();
        }
    }

    private HrRecurringPayDeductRequest createValidRequest() {
        HrRecurringPayDeductRequest request = new HrRecurringPayDeductRequest();
        request.setEmployeePoid(10L);
        request.setTransactionDate(LocalDate.now()); // Prevent NPE from DateUtil
        // Set total <= monthly to pass validation
        request.setTotalAmount(new BigDecimal("100.00"));
        request.setMonthlyAmount(new BigDecimal("1000.00"));
        request.setSettledAndClosed("N");
        return request;
    }

    @Test
    void testCreate_Success() {
        HrRecurringPayDeductRequest request = createValidRequest();
        when(loansAdvancesProcRepository.validateRecurring(any(), any(), any())).thenReturn("true");
        when(repository.saveAndFlush(any(HrRecurringPayDeduct.class))).thenAnswer(invocation -> {
            HrRecurringPayDeduct entity = invocation.getArgument(0);
            entity.setTransactionPoid(100L);
            return entity;
        });

        Long id = service.create(request);

        assertEquals(100L, id);
        verify(repository, times(1)).saveAndFlush(any(HrRecurringPayDeduct.class));
        verify(loggingService, times(1)).createLogSummaryEntry(eq("DOC-123"), eq("100"), anyString());
    }

    @Test
    void testCreate_ValidationFailure_Settled() {
        HrRecurringPayDeductRequest request = createValidRequest();
        request.setSettledAndClosed("Y"); // missing receipt no and date

        ValidationException exception = assertThrows(ValidationException.class, () -> service.create(request));
        assertEquals("Receipt No and Date required when settled", exception.getMessage());
    }

    @Test
    void testCreate_ValidationFailure_BusinessRule1() {
        HrRecurringPayDeductRequest request = createValidRequest();
        request.setTotalAmount(new BigDecimal("1000.00"));
        request.setMonthlyAmount(new BigDecimal("100.00")); // total > monthly causes validation failure

        ValidationException exception = assertThrows(ValidationException.class, () -> service.create(request));
        assertEquals("Total Amount should not be greater than Monthly Amount", exception.getMessage());
    }

    @Test
    void testCreate_ValidationFailure_Proc() {
        HrRecurringPayDeductRequest request = createValidRequest();
        when(loansAdvancesProcRepository.validateRecurring(any(), any(), any())).thenReturn("false");

        ValidationException exception = assertThrows(ValidationException.class, () -> service.create(request));
        assertEquals("Validation failed: false", exception.getMessage());
    }

    @Test
    void testCreate_ValidationFailure_Settled_MissingReceiptDateOnly() {
        HrRecurringPayDeductRequest request = createValidRequest();
        request.setSettledAndClosed("Y");
        request.setReceiptNo("REC-123");
        request.setReceiptDate(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> service.create(request));
        assertEquals("Receipt No and Date required when settled", exception.getMessage());
    }

    @Test
    void testCreate_Success_SettledAndValid() {
        HrRecurringPayDeductRequest request = createValidRequest();
        request.setSettledAndClosed("Y");
        request.setReceiptNo("REC-123");
        request.setReceiptDate(LocalDate.now());

        when(loansAdvancesProcRepository.validateRecurring(any(), any(), any())).thenReturn("true");
        when(repository.saveAndFlush(any(HrRecurringPayDeduct.class))).thenAnswer(invocation -> {
            HrRecurringPayDeduct entity = invocation.getArgument(0);
            entity.setTransactionPoid(100L);
            return entity;
        });

        Long id = service.create(request);
        assertEquals(100L, id);
    }

    @Test
    void testCreate_Success_TotalAmountNull() {
        HrRecurringPayDeductRequest request = createValidRequest();
        request.setTotalAmount(null); // Cover branch where totalAmount is null

        when(loansAdvancesProcRepository.validateRecurring(any(), any(), any())).thenReturn("true");
        when(repository.saveAndFlush(any(HrRecurringPayDeduct.class))).thenAnswer(invocation -> {
            HrRecurringPayDeduct entity = invocation.getArgument(0);
            entity.setTransactionPoid(101L);
            return entity;
        });

        Long id = service.create(request);
        assertEquals(101L, id);
    }

    @Test
    void testCreate_ValidationFailure_ProcNull() {
        HrRecurringPayDeductRequest request = createValidRequest();
        when(loansAdvancesProcRepository.validateRecurring(any(), any(), any())).thenReturn(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> service.create(request));
        assertEquals("Validation failed: null", exception.getMessage());
    }

    @Test
    void testGetById_Success() {
        HrRecurringPayDeduct entity = new HrRecurringPayDeduct();
        entity.setTransactionPoid(100L);
        when(repository.findByTransactionPoidDeleted(100L)).thenReturn(Optional.of(entity));

        HrRecurringPayDeductResponse response = service.getById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getTransactionPoid());
    }

    @Test
    void testGetById_NotFound() {
        when(repository.findByTransactionPoidDeleted(100L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.getById(100L));
        assertTrue(exception.getMessage().contains("Loans/Advances"));
    }

    @Test
    void testUpdate_Success() {
        HrRecurringPayDeductRequest request = createValidRequest();
        HrRecurringPayDeduct existingEntity = new HrRecurringPayDeduct();
        existingEntity.setTransactionPoid(100L);

        when(repository.findById(100L)).thenReturn(Optional.of(existingEntity));
        when(loansAdvancesProcRepository.validateRecurring(any(), any(), any())).thenReturn("true");
        when(repository.save(any(HrRecurringPayDeduct.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HrRecurringPayDeductResponse response = service.update(100L, request);

        assertNotNull(response);
        assertEquals(100L, response.getTransactionPoid());
        verify(repository, times(1)).save(any(HrRecurringPayDeduct.class));
        verify(loggingService, times(1)).logChanges(any(), any(), eq(HrRecurringPayDeduct.class), eq("DOC-123"), eq("100"), eq(LogDetailsEnum.MODIFIED), eq("TRANSACTION_POID"));
    }

    @Test
    void testUpdate_NotFound() {
        HrRecurringPayDeductRequest request = createValidRequest();
        when(repository.findById(100L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.update(100L, request));
        assertTrue(exception.getMessage().contains("Loans/Advances"));
    }

    @Test
    void testDelete_Success() {
        HrRecurringPayDeduct entity = new HrRecurringPayDeduct();
        entity.setTransactionPoid(100L);
        when(repository.findByTransactionPoidDeleted(100L)).thenReturn(Optional.of(entity));

        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();
        service.delete(100L, deleteReasonDto);

        verify(documentDeleteService, times(1)).deleteDocument(eq(100L), eq("HR_RECURRING_PAY_DEDUCT"), eq("TRANSACTION_POID"), eq(deleteReasonDto), isNull());
    }

    @Test
    void testDelete_NotFound() {
        when(repository.findByTransactionPoidDeleted(100L)).thenReturn(Optional.empty());

        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> service.delete(100L, deleteReasonDto));
        assertTrue(exception.getMessage().contains("Loans/Advances"));
    }

    @Test
    void testList() {
        FilterRequestDto filterRequest = new FilterRequestDto("AND", "N", List.of());
        Pageable pageable = PageRequest.of(0, 10);

        when(documentSearchService.resolveOperator(filterRequest)).thenReturn("AND");
        when(documentSearchService.resolveIsDeleted(filterRequest)).thenReturn("N");
        List<FilterDto> mockFilters = Collections.emptyList();
        when(documentSearchService.resolveFilters(filterRequest)).thenReturn(mockFilters);

        List<Map<String, Object>> records = List.of(Map.of("id", 1));
        Map<String, String> displayFields = Map.of("id", "ID");
        RawSearchResult rawResult = new RawSearchResult(records, displayFields, 1L);

        when(documentSearchService.search(eq("DOC-123"), eq(mockFilters), eq("AND"), eq(pageable), eq("N"), eq("DESCRIPTION"), eq("TRANSACTION_POID")))
                .thenReturn(rawResult);

        Map<String, Object> result = service.list(filterRequest, pageable);

        assertNotNull(result);
        assertTrue(result.containsKey("content"));
        verify(documentSearchService, times(1)).search(any(), any(), any(), any(), any(), any(), any());
    }
}