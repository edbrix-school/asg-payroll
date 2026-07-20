package com.asg.payroll.salarydetails.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.GlobalParameterService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.PrintService;
import com.asg.payroll.common.util.ActionType;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryMaster;
import com.asg.payroll.salarydetails.repository.HrEmployeeSalaryMasterRepository;
import com.asg.payroll.exceptions.ValidationException;
import com.asg.payroll.salarydetails.dto.SalaryAllowanceDto;
import com.asg.payroll.salarydetails.dto.SalaryDetailRequest;
import com.asg.payroll.salarydetails.dto.SalaryDetailResponse;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryAlwDtl;
import com.asg.payroll.salarydetails.repository.HrEmployeeSalaryAlwDtlRepository;
import com.asg.payroll.salarydetails.repository.HrEmployeeSalaryHistRepository;
import com.asg.payroll.salarydetails.repository.HrEmployeeSalaryProcRepository;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryDetailsServiceImplTest {

    @Mock
    private HrEmployeeSalaryMasterRepository repository;
    @Mock
    private HrEmployeeSalaryAlwDtlRepository alwDtlRepository;
    @Mock
    private HrEmployeeSalaryHistRepository histRepository;
    @Mock
    private HrEmployeeSalaryProcRepository procRepository;
    @Mock
    private LoggingService loggingService;
    @Mock
    private DocumentDeleteService documentDeleteService;
    @Mock
    private DocumentSearchService documentSearchService;
    @Mock
    private PrintService printService;
    @Mock
    private DataSource dataSource;
    @Mock
    private GlobalParameterService globalParameterService;

    @InjectMocks
    private SalaryDetailsServiceImpl service;

    private MockedStatic<UserContext> userContextMockedStatic;

    @BeforeEach
    void setUp() {
        userContextMockedStatic = Mockito.mockStatic(UserContext.class);
        userContextMockedStatic.when(UserContext::getDocumentId).thenReturn("DOC-123");
        userContextMockedStatic.when(UserContext::getCompanyPoid).thenReturn(1L);
        userContextMockedStatic.when(UserContext::getUserPoid).thenReturn(101L);
        userContextMockedStatic.when(UserContext::getGroupPoid).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        if (userContextMockedStatic != null) {
            userContextMockedStatic.close();
        }
    }

    @Test
    void testUpdate_Success() {
        Long id = 1L;
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        request.setIbanAccountNo("1234567890123456789012"); // 22 chars

        SalaryAllowanceDto alw1 = SalaryAllowanceDto.builder()
                .actionType(ActionType.ISCREATED)
                .amount(BigDecimal.valueOf(500L))
                .active("1")
                .build();
        request.setAllowances(List.of(alw1));

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(id);
        entity.setEmployeePoid(100L);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(globalParameterService.getParameterValue(eq("Payroll_IBANValidation"), any(), any(), any())).thenReturn("Y");
        when(alwDtlRepository.getMaxDetRowId(id)).thenReturn(10L);

        HrEmployeeSalaryAlwDtl savedAlw = new HrEmployeeSalaryAlwDtl();
        savedAlw.setDetRowId(11L);
        when(alwDtlRepository.saveAll(any())).thenReturn(List.of(savedAlw));
        when(alwDtlRepository.findBySalaryPoid(id)).thenReturn(new ArrayList<>());
        when(histRepository.findBySalaryPoid(id)).thenReturn(new ArrayList<>());
        when(procRepository.getEmployeeDetails(100L)).thenReturn(Map.of("DESIGNATION_NAME", "Manager"));

        SalaryDetailResponse response = service.update(id, request);

        assertNotNull(response);
        verify(repository, atLeastOnce()).save(any());
        verify(alwDtlRepository).saveAll(any());
        verify(loggingService).logChanges(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testUpdate_NoAllowances() {
        Long id = 1L;
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        request.setAllowances(null);

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(id);
        entity.setEmployeePoid(100L);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        service.update(id, request);

        verify(alwDtlRepository, never()).saveAll(any());
    }

    @Test
    void testUpdate_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, new SalaryDetailRequest()));
    }

    @Test
    void testUpdate_ValidationFailures() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        when(repository.findById(anyLong())).thenReturn(Optional.of(new HrEmployeeSalaryMaster()));

        // Employee mandatory
        assertThrows(ValidationException.class, () -> service.update(1L, request));

        request.setEmployeePoid(100L);
        // Payment method mandatory
        assertThrows(ValidationException.class, () -> service.update(1L, request));

        request.setPaymentMethod("bank");
        // Bank mandatory if payment is bank
        assertThrows(ValidationException.class, () -> service.update(1L, request));

        request.setBankPoid(1L);
        // Bank Reg ID mandatory if payment is bank
        assertThrows(ValidationException.class, () -> service.update(1L, request));

        request.setBankRegistrationId("REG-1");
        request.setIbanAccountNo("too-short");
        // IBAN length check — param enabled by default
        when(globalParameterService.getParameterValue(eq("Payroll_IBANValidation"), any(), any(), any())).thenReturn("Y");
        assertThrows(ValidationException.class, () -> service.update(1L, request));
    }

    @Test
    void testUpdate_AllowanceActions() {
        Long id = 1L;
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");

        SalaryAllowanceDto alwCreate = SalaryAllowanceDto.builder().actionType(ActionType.ISCREATED).amount(BigDecimal.valueOf(100L)).active("1").build();
        SalaryAllowanceDto alwUpdate = SalaryAllowanceDto.builder().actionType(ActionType.ISUPDATED).detRowId(20L).amount(BigDecimal.valueOf(200L)).active("1").build();
        SalaryAllowanceDto alwDelete = SalaryAllowanceDto.builder().actionType(ActionType.ISDELETED).detRowId(30L).build();
        request.setAllowances(List.of(alwCreate, alwUpdate, alwDelete));

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(id);
        entity.setEmployeePoid(100L);
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        HrEmployeeSalaryAlwDtl existingAlw = new HrEmployeeSalaryAlwDtl();
        existingAlw.setDetRowId(20L);
        when(alwDtlRepository.findBySalaryPoidAndDetRowId(id, 20L)).thenReturn(Optional.of(existingAlw));

        service.update(id, request);

        verify(alwDtlRepository, times(2)).saveAll(any());
        verify(alwDtlRepository).deleteBySalaryPoidAndDetRowIdIn(eq(id), any());
        verify(loggingService).logDelete(any(), any(), any());
        verify(loggingService).createLogBatch(any());
    }

    @Test
    void testGetById_Success() {
        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        entity.setEmployeePoid(100L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Map<String, Object> empDetails = Map.of(
                "TICKET_DETAILS", "2 Tickets",
                "DESIGNATION_NAME", "Developer",
                "JOIN_DATE", Timestamp.valueOf(LocalDateTime.now())
        );
        when(procRepository.getEmployeeDetails(100L)).thenReturn(empDetails);

        SalaryDetailResponse response = service.getById(1L);

        assertNotNull(response);
        assertEquals("Developer", response.getDesignation());
        assertEquals("2 Tickets", response.getTicketDetails());
    }

    @Test
    void testGetById_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void testDelete_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(new HrEmployeeSalaryMaster()));
        service.delete(1L, new DeleteReasonDto());
        verify(documentDeleteService).deleteDocument(eq(1L), any(), any(), any(), any());
    }

    @Test
    void testList() {
        FilterRequestDto filterRequest = new FilterRequestDto("AND", "N", new ArrayList<>());
        when(documentSearchService.resolveOperator(any())).thenReturn("AND");
        when(documentSearchService.resolveIsDeleted(any())).thenReturn("N");
        when(documentSearchService.resolveFilters(any())).thenReturn(new ArrayList<>());

        RawSearchResult raw = new RawSearchResult(List.of(Map.of("name", "John")), Map.of(), 1L);
        when(documentSearchService.search(any(), any(), any(), any(), any(), any(), any())).thenReturn(raw);

        Map<String, Object> result = service.list(filterRequest, Pageable.unpaged());

        assertNotNull(result);
        assertTrue(result.containsKey("content"));
    }

    @Test
    void testIbanValidation_DisabledByParam() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        request.setIbanAccountNo("short"); // invalid length but param disabled

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(globalParameterService.getParameterValue(eq("Payroll_IBANValidation"), any(), any(), any())).thenReturn("N");
        when(alwDtlRepository.findBySalaryPoid(1L)).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> service.update(1L, request));
    }

    @Test
    void testAddToHistory() {
        when(repository.findById(1L)).thenReturn(Optional.of(new HrEmployeeSalaryMaster()));
        when(procRepository.addToSalaryHistory(any(), any(), any())).thenReturn("SUCCESS");
        String result = service.addToHistory(1L);
        assertEquals("SUCCESS", result);
    }

    @Test
    void testAddToHistory_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.addToHistory(99L));
    }

    @Test
    void testSyncHRData() {
        when(procRepository.syncHRData()).thenReturn("SUCCESS");
        String result = service.syncHRData();
        assertEquals("SUCCESS", result);
        verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.MODIFIED), any(), any());
    }

    @Test
    void testCalculateCTC() {
        when(procRepository.calculateCTC(any(), any(), any())).thenReturn("5000");
        String result = service.calculateCTC(100L);
        assertEquals("5000", result);
    }

    @Test
    void testPrintOfferLetter() throws Exception {
        when(printService.buildBaseParams(any(), any())).thenReturn(Map.of());
        when(printService.load(any())).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), any(), any())).thenReturn(new byte[]{1, 2});

        byte[] result = service.printOfferLetter(1L);
        assertArrayEquals(new byte[]{1, 2}, result);
    }

    @Test
    void testPrintSalaryCertificate() throws Exception {
        when(printService.buildBaseParams(any(), any())).thenReturn(Map.of());
        when(printService.load(any())).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), any(), any())).thenReturn(new byte[]{1, 2});

        byte[] result = service.printSalaryCertificate(1L);
        assertArrayEquals(new byte[]{1, 2}, result);
    }

    @Test
    void testPrintContract_AllTypes() throws Exception {
        when(printService.buildBaseParams(any(), any())).thenReturn(Map.of());
        when(printService.load(any())).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), any(), any())).thenReturn(new byte[]{1, 2});

        String[] types = {"EXPAT_OPEN", "EXPAT_LTD", "BAHRAINI_OPEN", "BAHRAINI_LTD"};
        for (String type : types) {
            byte[] result = service.printContract(1L, type);
            assertArrayEquals(new byte[]{1, 2}, result);
        }
    }

    @Test
    void testPrintContract_NullType_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> service.printContract(1L, null));
    }

    @Test
    void testPrintContract_BlankType_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> service.printContract(1L, "  "));
    }

    @Test
    void testPrintContract_UnknownType_ThrowsValidationException() throws Exception {
        when(printService.buildBaseParams(any(), any())).thenReturn(Map.of());
        assertThrows(ValidationException.class, () -> service.printContract(1L, "UNKNOWN_TYPE"));
    }

    @Test
    void testPrintAnnex() throws Exception {
        when(printService.buildBaseParams(any(), any())).thenReturn(Map.of());
        when(printService.load("HR/Employee_Contract_Annex_one.jrxml")).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), any(), any())).thenReturn(new byte[]{3, 4});

        byte[] result = service.printAnnex(1L);

        assertArrayEquals(new byte[]{3, 4}, result);
        verify(printService).load("HR/Employee_Contract_Annex_one.jrxml");
    }

    @Test
    void testPrintEmployeeDetails_PreviewTrue() throws Exception {
        when(printService.buildBaseParams(any(), any())).thenReturn(new java.util.HashMap<>());
        when(printService.load("HR/EmployeeDetailsReportWithSalary.jrxml")).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), any(), any())).thenReturn(new byte[]{5, 6});

        byte[] result = service.printEmployeeDetails(1L, true);

        assertArrayEquals(new byte[]{5, 6}, result);
        verify(printService).load("HR/EmployeeDetailsReportWithSalary.jrxml");
    }

    @Test
    void testPrintEmployeeDetails_PreviewFalse() throws Exception {
        when(printService.buildBaseParams(any(), any())).thenReturn(new java.util.HashMap<>());
        when(printService.load("HR/EmployeeDetailsReportWithSalary.jrxml")).thenReturn(mock(JasperReport.class));
        when(printService.fillReportToPdf(any(), any(), any())).thenReturn(new byte[]{7, 8});

        byte[] result = service.printEmployeeDetails(1L, false);

        assertArrayEquals(new byte[]{7, 8}, result);
    }

    @Test
    void testGetSalaryRevisions_Success() {
        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        entity.setEmployeePoid(100L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(histRepository.findBySalaryPoid(1L)).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.getSalaryRevisions(1L);

        assertNotNull(result);
        assertEquals(100L, result.get("EMPLOYEE_POID"));
        assertTrue(result.containsKey("revisions"));
        verify(histRepository).findBySalaryPoid(1L);
    }

    @Test
    void testGetSalaryRevisions_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getSalaryRevisions(99L));
    }

    @Test
    void testEnableSalaryEdit_LogsEntry() {
        service.enableSalaryEdit(1L);
        verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.MODIFIED), eq("DOC-123"), contains("1"));
    }

    @Test
    void testCalculateTotals_NetSalaryEqualsGrossWhenNoDeductions() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        request.setBasicSalary(BigDecimal.valueOf(1000));
        SalaryAllowanceDto alw = SalaryAllowanceDto.builder()
                .actionType(ActionType.ISCREATED)
                .allowanceDeductionPoid(47L)
                .amount(BigDecimal.valueOf(300))
                .active("Y")
                .build();
        request.setAllowances(List.of(alw));

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenReturn(entity);
        when(alwDtlRepository.findBySalaryPoid(1L)).thenReturn(new ArrayList<>());
        when(procRepository.getAllowanceDeductionTypes(any())).thenReturn(Map.of(47L, "ALLOWANCE"));

        service.update(1L, request);

        // gross = 1000 + 300 = 1300, net = gross - 0 deductions = 1300
        assertEquals(new BigDecimal("1300"), entity.getGrossSalary());
        assertEquals(new BigDecimal("1300"), entity.getNetSalary());
    }

    @Test
    void testCalculateTotals_ClassifiesByMasterType() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        request.setBasicSalary(BigDecimal.valueOf(1000));

        // allowance row (TYPE=ALLOWANCE) comes from the request
        SalaryAllowanceDto alw = SalaryAllowanceDto.builder()
                .actionType(ActionType.ISCREATED)
                .allowanceDeductionPoid(47L)
                .amount(BigDecimal.valueOf(300))
                .active("Y")
                .build();
        request.setAllowances(List.of(alw));

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenReturn(entity);

        // deduction row (TYPE=DEDUCTION) comes from the persisted rows
        HrEmployeeSalaryAlwDtl deduction = new HrEmployeeSalaryAlwDtl();
        deduction.setAllowanceDeductionPoid(81L);
        deduction.setAmount(BigDecimal.valueOf(100));
        when(alwDtlRepository.findBySalaryPoid(1L)).thenReturn(new ArrayList<>(List.of(deduction)));

        when(procRepository.getAllowanceDeductionTypes(any()))
                .thenReturn(Map.of(47L, "ALLOWANCE", 81L, "DEDUCTION"));

        service.update(1L, request);

        // gross = 1000 + 300 allowance = 1300 ; net = 1300 - 100 deduction = 1200
        assertEquals(new BigDecimal("300"), entity.getTotAllowance());
        assertEquals(new BigDecimal("1300"), entity.getGrossSalary());
        assertEquals(new BigDecimal("1200"), entity.getNetSalary());
    }

    @Test
    void testBuildSalaryResponse_JoinDateLocalDateTime() {
        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        entity.setEmployeePoid(100L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Map<String, Object> empDetails = Map.of(
                "JOIN_DATE", LocalDateTime.now()
        );
        when(procRepository.getEmployeeDetails(100L)).thenReturn(empDetails);

        SalaryDetailResponse response = service.getById(1L);
        assertNotNull(response.getJoinDate());
    }

    @Test
    void testUpdate_AllowanceNotFound() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        SalaryAllowanceDto alwDto = new SalaryAllowanceDto();
        alwDto.setDetRowId(999L);
        alwDto.setActionType(ActionType.ISUPDATED);
        request.setAllowances(List.of(alwDto));

        when(repository.findById(1L)).thenReturn(Optional.of(new HrEmployeeSalaryMaster()));
        when(alwDtlRepository.findBySalaryPoidAndDetRowId(anyLong(), eq(999L))).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> service.update(1L, request));
    }

    @Test
    void testUpdate_EmptyAllowances() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        request.setAllowances(new ArrayList<>());

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenReturn(entity);

        SalaryDetailResponse response = service.update(1L, request);
        assertNotNull(response);
    }

    @Test
    void testCalculateTotals_EdgeCases() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");

        SalaryAllowanceDto alw1 = new SalaryAllowanceDto();
        SalaryAllowanceDto alw2 = new SalaryAllowanceDto();
        alw2.setActive("0");
        alw2.setAmount(BigDecimal.valueOf(100));

        request.setAllowances(List.of(alw1, alw2));

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        entity.setBasicSalary(null);

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenReturn(entity);

        service.update(1L, request);
        assertEquals(BigDecimal.ZERO, entity.getTotAllowance());
        assertEquals(BigDecimal.ZERO, entity.getGrossSalary());
    }

    @Test
    void testProcessAllowanceDto_UnknownActionType() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        SalaryAllowanceDto alwDto = new SalaryAllowanceDto();
        alwDto.setActionType(null);
        request.setAllowances(List.of(alwDto));

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenReturn(entity);

        service.update(1L, request);
        verify(alwDtlRepository, never()).saveAll(any());
    }

    @Test
    void testMaxDetRowId_NullFallback() {
        when(alwDtlRepository.getMaxDetRowId(1L)).thenReturn(null);

        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        SalaryAllowanceDto alwDto = new SalaryAllowanceDto();
        alwDto.setActionType(ActionType.ISCREATED);
        request.setAllowances(List.of(alwDto));

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenReturn(entity);

        service.update(1L, request);
        verify(alwDtlRepository, atLeastOnce()).saveAll(anyList());
    }

    @Test
    void testUpdate_Validation_EdgeCases() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH"); // Not bank
        request.setIbanAccountNo(null); // Null IBAN

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setSalaryPoid(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenReturn(entity);

        SalaryDetailResponse response = service.update(1L, request);
        assertNotNull(response);
    }

    @Test
    void testUpdate_OnlyCreated() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        SalaryAllowanceDto alw = SalaryAllowanceDto.builder().actionType(ActionType.ISCREATED).amount(BigDecimal.valueOf(100)).build();
        request.setAllowances(List.of(alw));

        when(repository.findById(1L)).thenReturn(Optional.of(new HrEmployeeSalaryMaster()));
        when(repository.save(any())).thenReturn(new HrEmployeeSalaryMaster());

        service.update(1L, request);
        verify(alwDtlRepository).saveAll(any());
        verify(alwDtlRepository, never()).deleteBySalaryPoidAndDetRowIdIn(any(), any());
    }

    @Test
    void testUpdate_OnlyUpdated() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        SalaryAllowanceDto alw = SalaryAllowanceDto.builder().actionType(ActionType.ISUPDATED).detRowId(1L).amount(BigDecimal.valueOf(100)).build();
        request.setAllowances(List.of(alw));

        when(repository.findById(1L)).thenReturn(Optional.of(new HrEmployeeSalaryMaster()));
        when(repository.save(any())).thenReturn(new HrEmployeeSalaryMaster());
        when(alwDtlRepository.findBySalaryPoidAndDetRowId(any(), any())).thenReturn(Optional.of(new HrEmployeeSalaryAlwDtl()));

        service.update(1L, request);
        verify(alwDtlRepository).saveAll(any());
        verify(loggingService).createLogBatch(any());
    }

    @Test
    void testUpdate_OnlyDeleted() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setEmployeePoid(100L);
        request.setPaymentMethod("CASH");
        SalaryAllowanceDto alw = SalaryAllowanceDto.builder().actionType(ActionType.ISDELETED).detRowId(1L).build();
        request.setAllowances(List.of(alw));

        when(repository.findById(1L)).thenReturn(Optional.of(new HrEmployeeSalaryMaster()));
        when(repository.save(any())).thenReturn(new HrEmployeeSalaryMaster());

        service.update(1L, request);
        verify(alwDtlRepository).deleteBySalaryPoidAndDetRowIdIn(any(), any());
        verify(alwDtlRepository, never()).saveAll(any());
    }
}