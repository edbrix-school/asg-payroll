package com.asg.payroll.employeeSettlement.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.employeeSettlement.dto.EmployeeSettlementDto;
import com.asg.payroll.employeeSettlement.entity.EmployeeSettlementDtl;
import com.asg.payroll.employeeSettlement.service.EmployeeSettlementService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeSettlementControllerTest {

    @Mock
    private EmployeeSettlementService service;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private EmployeeSettlementController controller;

    private MockedStatic<UserContext> userContextMockedStatic;

    @BeforeEach
    void setUp() {
        userContextMockedStatic = Mockito.mockStatic(UserContext.class);
        userContextMockedStatic.when(UserContext::getDocumentId).thenReturn("DOC-001");
    }

    @AfterEach
    void tearDown() {
        if (userContextMockedStatic != null) {
            userContextMockedStatic.close();
        }
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

    // --- GET by ID ---

    @Test
    void testGetEmployeeDetailById_Success() {
        EmployeeSettlementDto dto = buildSampleDto();
        when(service.getEmployeeSettlement(1L)).thenReturn(dto);

        ResponseEntity<?> response = controller.getEmployeeDetailById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).getEmployeeSettlement(1L);
    }

    // --- DELETE ---

    @Test
    void testDeleteEmployeeSettlement_Success() {
        doNothing().when(service).deleteEmployeeSettlement(eq(1L), any(DeleteReasonDto.class));

        ResponseEntity<?> response = controller.deleteEmployeeSettlement(1L, new DeleteReasonDto());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).deleteEmployeeSettlement(eq(1L), any(DeleteReasonDto.class));
    }

    // --- CREATE ---

    @Test
    void testCreateEmployeeSettlement_Success() {
        EmployeeSettlementDto dto = buildSampleDto();
        when(service.createEmployeeSettlement(any(EmployeeSettlementDto.class))).thenReturn(dto);

        ResponseEntity<?> response = controller.createEmployeeSettlement(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).createEmployeeSettlement(any(EmployeeSettlementDto.class));
    }

    // --- UPDATE ---

    @Test
    void testUpdateEmployeeSettlement_Success() {
        EmployeeSettlementDto dto = buildSampleDto();
        when(service.updateEmployeeSettlement(eq(1L), any(EmployeeSettlementDto.class))).thenReturn(dto);
        when(service.getEmployeeSettlement(1L)).thenReturn(dto);

        ResponseEntity<?> response = controller.updateEmployeeSettlement(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).updateEmployeeSettlement(eq(1L), any(EmployeeSettlementDto.class));
    }

    // --- SEARCH ---

    @Test
    void testSearchEmployeeSettlement_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> result = Map.of("content", java.util.List.of());
        when(service.searchEmployeeSettlement(anyString(), any(), any(Pageable.class), any(), any()))
                .thenReturn(result);

        ResponseEntity<?> response = controller.searchEmployeeSettlement(
                pageable, new FilterRequestDto(null, null, null), LocalDate.now().minusDays(7), LocalDate.now());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testSearchEmployeeSettlement_OnlyStartDate_ReturnsBadRequest() {
        Pageable pageable = PageRequest.of(0, 10);

        ResponseEntity<?> response = controller.searchEmployeeSettlement(
                pageable, null, LocalDate.now(), null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testSearchEmployeeSettlement_OnlyEndDate_ReturnsBadRequest() {
        Pageable pageable = PageRequest.of(0, 10);

        ResponseEntity<?> response = controller.searchEmployeeSettlement(
                pageable, null, null, LocalDate.now());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testSearchEmployeeSettlement_NoDates_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> result = Map.of("content", java.util.List.of());
        when(service.searchEmployeeSettlement(anyString(), any(), any(Pageable.class), isNull(), isNull()))
                .thenReturn(result);

        ResponseEntity<?> response = controller.searchEmployeeSettlement(pageable, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // --- ELIGIBLE LEAVE ---

    @Test
    void testGetEmployeeEligibleLeave_Success() {
        when(service.getEmployeeEligibleLeave(1L)).thenReturn(Map.of("eligibleDays", 15));

        ResponseEntity<?> response = controller.getEmployeeEligibileleave(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).getEmployeeEligibleLeave(1L);
    }

    // --- CREATE BPV ---

    @Test
    void testCreateSettlementBpv_Success() {
        EmployeeSettlementDtl request = new EmployeeSettlementDtl();
        request.setPaymentMethod("BANK");
        request.setPaymentBankPoid(5L);
        request.setPaymentPayeeName("John Doe");
        request.setPaymentValueDate(LocalDate.now());
        request.setPaymentPrePrinted("N");

        when(service.createSettlementBpv(anyLong(), anyString(), anyLong(), anyString(), any(), anyString()))
                .thenReturn("SUCCESS");

        ResponseEntity<?> response = controller.createSettlementBpv(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // --- CREATE BDV ---

    @Test
    void testCreateSettlementBdv_Success() {
        EmployeeSettlementDtl request = new EmployeeSettlementDtl();
        request.setPaymentMethod("BANK");
        request.setPaymentBankPoid(5L);
        request.setPaymentPayeeName("Jane Doe");
        request.setPaymentValueDate(LocalDate.now());
        request.setPaymentPrePrinted("N");

        when(service.createSettlementBdv(anyLong(), anyString(), anyLong(), anyString(), any(), anyString()))
                .thenReturn("SUCCESS");

        ResponseEntity<?> response = controller.createSettlementBdv(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // --- CREATE JV ---

    @Test
    void testCreateSettlementJV_Success() {
        when(service.createSettlementJv(1L)).thenReturn("JV-001");

        ResponseEntity<?> response = controller.createSettlementJV(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).createSettlementJv(1L);
    }

    // --- LEAVE DATES ---

    @Test
    void testGetEmployeeLeaveDates_Success() {
        Map<String, String> dates = Map.of("startDate", "2026-01-01", "rejoinDate", "2026-02-01");
        when(service.getEmployeeLeaveDates("100")).thenReturn(dates);

        ResponseEntity<?> response = controller.getEmployeeLeaveDates("100");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).getEmployeeLeaveDates("100");
    }

    // --- LEAVE REQUEST DETAILS ---

    @Test
    void testGetLeaveRequestDetails_Success() {
        Map<String, Object> details = Map.of("data", Map.of("leaveType", "ANNUAL"), "status", "SUCCESS");
        when(service.getLeaveRequestDetails(1L)).thenReturn(details);

        ResponseEntity<?> response = controller.getLeaveRequestDetails(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service).getLeaveRequestDetails(1L);
    }

    // --- SYNC HR DATA ---

    @Test
    void testSyncHRData_Success() {
        when(service.syncHRData()).thenReturn("SUCCESS: Sync completed");

        ResponseEntity<?> response = controller.syncHRData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testSyncHRData_Error() {
        when(service.syncHRData()).thenReturn("ERROR: Connection failed");

        ResponseEntity<?> response = controller.syncHRData();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testSyncHRData_NullStatus() {
        when(service.syncHRData()).thenReturn(null);

        ResponseEntity<?> response = controller.syncHRData();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // --- RECURRING PAYROLL ---

    @Test
    void testGetRecurringPayroll_Success() {
        Map<String, Object> result = Map.of("data", java.util.List.of());
        when(service.getRecurringToPayroll(anyLong(), any(), any(), any())).thenReturn(result);

        ResponseEntity<?> response = controller.getRecurringPayroll(1L, 2L, 100L, LocalDate.now());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // --- CALCULATE INDEMNITY ---

    @Test
    void testCalculateIndemnity_Success() {
        Map<String, Object> result = Map.of("status", "SUCCESS", "data", Map.of());
        when(service.calculateIndemnity(anyLong(), anyLong(), anyLong(), any(), anyLong(), anyString()))
                .thenReturn(result);

        ResponseEntity<?> response = controller.calculateIndemnity(
                10L, 1L, 100L, LocalDate.now(), 0L, "N");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // --- PROCESS LEAVE PAYROLL ---

    @Test
    void testProcessLeavePayroll_Success() {
        Map<String, Object> result = Map.of("status", "SUCCESS");
        when(service.processLeavePayroll(anyLong(), any(), any(), any(), anyLong(), any(), any(), anyLong()))
                .thenReturn(result);

        ResponseEntity<?> response = controller.processLeavePayroll(
                10L, 1L, 2L, 3L, 100L, LocalDate.now(), LocalDate.now().plusDays(30), 0L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
