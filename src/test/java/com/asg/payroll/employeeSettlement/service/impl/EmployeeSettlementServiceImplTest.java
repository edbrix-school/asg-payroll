package com.asg.payroll.employeeSettlement.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.service.PrintService;
import com.asg.payroll.employeeSettlement.dto.EmployeeSettlementDto;
import javax.sql.DataSource;
import com.asg.payroll.employeeSettlement.dto.LoanDeductionDto;
import com.asg.payroll.employeeSettlement.entity.EmployeeSettlementDtl;
import com.asg.payroll.employeeSettlement.entity.LoanDeductionDtl;
import com.asg.payroll.employeeSettlement.repository.EmployeeSettlementDtlRepository;
import com.asg.payroll.employeeSettlement.repository.LoanDeductionDtlRepository;
import com.asg.payroll.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeSettlementServiceImplTest {

    @Mock
    private EmployeeSettlementDtlRepository employeeSettlementDtlRepository;

    @Mock
    private LoanDeductionDtlRepository loanDeductionDtlRepository;

    @Mock
    private DocumentDeleteService documentDeleteService;

    @Mock
    private LoggingService loggingService;

    @Mock
    private LovDataService lovDataService;

    @Mock
    private PrintService printService;

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DocumentSearchService documentService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private EmployeeSettlementServiceImpl service;

    private MockedStatic<UserContext> userContextMockedStatic;

    @BeforeEach
    void setUp() {
        userContextMockedStatic = Mockito.mockStatic(UserContext.class);
        userContextMockedStatic.when(UserContext::getDocumentId).thenReturn("DOC-001");
        userContextMockedStatic.when(UserContext::getUserPoid).thenReturn(101L);
        userContextMockedStatic.when(UserContext::getCompanyPoid).thenReturn(10L);
        userContextMockedStatic.when(UserContext::getGroupPoid).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        if (userContextMockedStatic != null) {
            userContextMockedStatic.close();
        }
    }

    private EmployeeSettlementDtl buildSampleEntity() {
        EmployeeSettlementDtl entity = new EmployeeSettlementDtl();
        entity.setTransactionPoid(1L);
        entity.setEmployeePoid(100L);
        entity.setCompanyPoid(10L);
        entity.setGroupPoid(1L);
        entity.setSettlementType("FINAL");
        entity.setLeaveType("ANNUAL");
        entity.setBasicSalary(BigDecimal.valueOf(5000));
        entity.setTransactionDate(LocalDate.now());
        entity.setLeaveStartDate(LocalDate.now().minusDays(30));
        entity.setLeaveEndDate(LocalDate.now());
        entity.setDeleted("N");
        return entity;
    }

    private EmployeeSettlementDto buildSampleDto() {
        return EmployeeSettlementDto.builder()
                .transactionPoid(1L)
                .employeePoid(100L)
                .companyPoid(10L)
                .groupPoid(1L)
                .settlementType("FINAL")
                .leaveType("ANNUAL")
                .basicSalary(BigDecimal.valueOf(5000))
                .transactionDate(LocalDate.now())
                .leaveStartDate(LocalDate.now().minusDays(30))
                .leaveEndDate(LocalDate.now())
                .build();
    }

    // --- GET ---

    @Test
    void testGetEmployeeSettlement_Success() {
        EmployeeSettlementDtl entity = buildSampleEntity();
        when(employeeSettlementDtlRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(entity));
        when(loanDeductionDtlRepository.findByTransactionPoidOrderByDetRowId(1L)).thenReturn(List.of());

        EmployeeSettlementDto result = service.getEmployeeSettlement(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTransactionPoid());
        verify(employeeSettlementDtlRepository).findByTransactionPoid(1L);
    }

    @Test
    void testGetEmployeeSettlement_NotFound_ThrowsException() {
        when(employeeSettlementDtlRepository.findByTransactionPoid(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getEmployeeSettlement(99L));
    }

    @Test
    void testGetEmployeeSettlement_WithLoanDeductions() {
        EmployeeSettlementDtl entity = buildSampleEntity();
        LoanDeductionDtl loan = new LoanDeductionDtl();
        loan.setDetRowId(1L);
        loan.setTransactionPoid(1L);
        loan.setRecurAmount(BigDecimal.valueOf(200));

        when(employeeSettlementDtlRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(entity));
        when(loanDeductionDtlRepository.findByTransactionPoidOrderByDetRowId(1L)).thenReturn(List.of(loan));

        EmployeeSettlementDto result = service.getEmployeeSettlement(1L);

        assertNotNull(result);
        assertNotNull(result.getLoanDeductionDetails());
        assertFalse(result.getLoanDeductionDetails().isEmpty());
    }

    // --- CREATE ---

    @Test
    void testCreateEmployeeSettlement_Success() {
        EmployeeSettlementDto dto = buildSampleDto();
        EmployeeSettlementDtl savedEntity = buildSampleEntity();

        when(employeeSettlementDtlRepository.saveAndFlush(any(EmployeeSettlementDtl.class)))
                .thenAnswer(invocation -> {
                    EmployeeSettlementDtl arg = invocation.getArgument(0);
                    arg.setTransactionPoid(1L);
                    return arg;
                });
        doNothing().when(entityManager).refresh(any());
        when(employeeSettlementDtlRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(savedEntity));
        when(loanDeductionDtlRepository.findByTransactionPoidOrderByDetRowId(1L)).thenReturn(List.of());

        EmployeeSettlementDto result = service.createEmployeeSettlement(dto);

        assertNotNull(result);
        verify(employeeSettlementDtlRepository).saveAndFlush(any(EmployeeSettlementDtl.class));
    }

    @Test
    void testCreateEmployeeSettlement_WithLoanDeductions() {
        LoanDeductionDto loanDto = LoanDeductionDto.builder()
                .employeePoid(100L)
                .recurAmount(BigDecimal.valueOf(300))
                .recurType("LOAN")
                .build();

        EmployeeSettlementDto dto = buildSampleDto();
        dto.setLoanDeductionDetails(List.of(loanDto));

        EmployeeSettlementDtl savedEntity = buildSampleEntity();

        when(employeeSettlementDtlRepository.saveAndFlush(any(EmployeeSettlementDtl.class)))
                .thenAnswer(invocation -> {
                    EmployeeSettlementDtl arg = invocation.getArgument(0);
                    arg.setTransactionPoid(1L);
                    return arg;
                });
        doNothing().when(entityManager).refresh(any());
        when(loanDeductionDtlRepository.saveAll(anyList())).thenReturn(List.of());
        when(employeeSettlementDtlRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(savedEntity));
        when(loanDeductionDtlRepository.findByTransactionPoidOrderByDetRowId(1L)).thenReturn(List.of());

        EmployeeSettlementDto result = service.createEmployeeSettlement(dto);

        assertNotNull(result);
        verify(loanDeductionDtlRepository).saveAll(anyList());
    }

    // --- UPDATE ---

    @Test
    void testUpdateEmployeeSettlement_Success() {
        EmployeeSettlementDtl entity = buildSampleEntity();
        EmployeeSettlementDto dto = buildSampleDto();
        dto.setSettlementType("LEAVE");

        when(employeeSettlementDtlRepository.findByTransactionPoid(1L))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.of(entity));
        when(employeeSettlementDtlRepository.save(any(EmployeeSettlementDtl.class))).thenReturn(entity);
        when(loanDeductionDtlRepository.findByTransactionPoidOrderByDetRowId(1L)).thenReturn(List.of());

        EmployeeSettlementDto result = service.updateEmployeeSettlement(1L, dto);

        assertNotNull(result);
        verify(employeeSettlementDtlRepository).save(any(EmployeeSettlementDtl.class));
    }

    @Test
    void testUpdateEmployeeSettlement_NotFound_ThrowsException() {
        when(employeeSettlementDtlRepository.findByTransactionPoid(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateEmployeeSettlement(99L, buildSampleDto()));
    }

    @Test
    void testUpdateEmployeeSettlement_DeletedRecord_ThrowsException() {
        EmployeeSettlementDtl entity = buildSampleEntity();
        entity.setDeleted("Y");

        when(employeeSettlementDtlRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(entity));

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateEmployeeSettlement(1L, buildSampleDto()));
    }

    @Test
    void testUpdateEmployeeSettlement_WithLoanDeductions() {
        EmployeeSettlementDtl entity = buildSampleEntity();
        LoanDeductionDto loanDto = LoanDeductionDto.builder()
                .employeePoid(100L)
                .recurAmount(BigDecimal.valueOf(500))
                .recurType("LOAN")
                .build();
        EmployeeSettlementDto dto = buildSampleDto();
        dto.setLoanDeductionDetails(List.of(loanDto));

        when(employeeSettlementDtlRepository.findByTransactionPoid(1L))
                .thenReturn(Optional.of(entity))
                .thenReturn(Optional.of(entity));
        when(employeeSettlementDtlRepository.save(any())).thenReturn(entity);
        doNothing().when(loanDeductionDtlRepository).deleteByTransactionPoid(1L);
        when(loanDeductionDtlRepository.saveAll(anyList())).thenReturn(List.of());
        when(loanDeductionDtlRepository.findByTransactionPoidOrderByDetRowId(1L)).thenReturn(List.of());

        EmployeeSettlementDto result = service.updateEmployeeSettlement(1L, dto);

        assertNotNull(result);
        verify(loanDeductionDtlRepository).deleteByTransactionPoid(1L);
        verify(loanDeductionDtlRepository).saveAll(anyList());
    }

    // --- DELETE ---

    @Test
    void testDeleteEmployeeSettlement_Success() {
        EmployeeSettlementDtl entity = buildSampleEntity();
        when(employeeSettlementDtlRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(entity));
        when(documentDeleteService.deleteDocument(anyLong(), anyString(), anyString(), any(), any())).thenReturn("DELETED");

        assertDoesNotThrow(() -> service.deleteEmployeeSettlement(1L, new DeleteReasonDto()));
        verify(documentDeleteService).deleteDocument(eq(1L), eq("HR_LEAVE_SETTLEMENT_HDR"), eq("TRANSACTION_POID"), any(), any());
    }

    @Test
    void testDeleteEmployeeSettlement_NotFound_ThrowsException() {
        when(employeeSettlementDtlRepository.findByTransactionPoid(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteEmployeeSettlement(99L, new DeleteReasonDto()));
    }

    // --- SEARCH ---

    @Test
    void testSearchEmployeeSettlement_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        FilterRequestDto filterRequest = new FilterRequestDto(null, null, null);
        RawSearchResult rawResult = new RawSearchResult(List.of(Map.of("TRANSACTION_POID", 1L)), Map.of(), 1L);

        when(documentService.resolveOperator(any())).thenReturn("AND");
        when(documentService.resolveIsDeleted(any())).thenReturn("N");
        when(documentService.resolveDateFilters(any(), anyString(), any(), any())).thenReturn(List.of());
        when(documentService.search(anyString(), anyList(), anyString(), any(Pageable.class), anyString(), anyString(), anyString()))
                .thenReturn(rawResult);

        Map<String, Object> result = service.searchEmployeeSettlement(
                "DOC-001", filterRequest, pageable, LocalDate.now().minusDays(7), LocalDate.now());

        assertNotNull(result);
    }
}
