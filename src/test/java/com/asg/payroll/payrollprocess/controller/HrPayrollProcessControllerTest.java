package com.asg.payroll.payrollprocess.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDownloadHeaderService;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.payrollprocess.dto.*;
import com.asg.payroll.payrollprocess.service.HrPayrollProcessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrPayrollProcessControllerTest {

    @Mock
    private HrPayrollProcessService hrPayrollProcessService;

    @Mock
    private LoggingService loggingService;

    @Spy

    private DocumentDownloadHeaderService downloadHeaderService =

            new DocumentDownloadHeaderService(mock(JdbcTemplate.class));


    @InjectMocks
    private HrPayrollProcessController controller;

    private HrPayrollHdrResponse mockResponse() {
        return new HrPayrollHdrResponse();
    }

    @Test
    void listPayrolls_Success() {
        when(hrPayrollProcessService.listPayrolls(any(), any(), any(), any(), any())).thenReturn(new HashMap<>());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            ResponseEntity<?> response = controller.listPayrolls(
                    new FilterRequestDto("AND", "N", List.of()), PageRequest.of(0, 10), null, null);

            assertEquals(200, response.getStatusCode().value());
        }
    }

    @Test
    void getPayrollById_Success() {
        Long id = 1L;
        when(hrPayrollProcessService.getPayrollById(id)).thenReturn(mockResponse());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            ResponseEntity<?> response = controller.getPayrollById(id);

            assertEquals(200, response.getStatusCode().value());
            verify(loggingService).createLogSummaryEntry(any(LogDetailsEnum.class), anyString(), anyString());
        }
    }

    @Test
    void createPayroll_Success() {
        HrPayrollHdrRequest request = new HrPayrollHdrRequest();
        request.setAttendTranPoid(10L);
        request.setPayrollMonth(LocalDate.of(2024, 1, 31));

        when(hrPayrollProcessService.createPayroll(any())).thenReturn(mockResponse());

        ResponseEntity<?> response = controller.createPayroll(request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updatePayroll_Success() {
        Long id = 1L;
        HrPayrollHdrRequest request = new HrPayrollHdrRequest();
        request.setAttendTranPoid(10L);
        request.setPayrollMonth(LocalDate.of(2024, 1, 31));

        when(hrPayrollProcessService.updatePayroll(eq(id), any())).thenReturn(mockResponse());

        ResponseEntity<?> response = controller.updatePayroll(id, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void deletePayroll_Success() {
        Long id = 1L;
        doNothing().when(hrPayrollProcessService).deletePayroll(eq(id), any());

        ResponseEntity<?> response = controller.deletePayroll(id, new DeleteReasonDto());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void processPayroll_Success() {
        Long id = 1L;
        PayrollActionRequest request = new PayrollActionRequest();

        when(hrPayrollProcessService.processPayroll(eq(id), any(PayrollActionRequest.class))).thenReturn(new PayrollActionResponse());

        ResponseEntity<?> response = controller.processPayroll(id, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void processProvision_Success() {
        Long id = 1L;

        when(hrPayrollProcessService.processProvision(eq(id), any(PayrollActionRequest.class), eq("N"))).thenReturn(new PayrollActionResponse());

        ResponseEntity<?> response = controller.processProvision(id, new PayrollActionRequest(), "N");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void processProvision_WithPostJvY_Success() {
        Long id = 1L;

        when(hrPayrollProcessService.processProvision(eq(id), any(PayrollActionRequest.class), eq("Y"))).thenReturn(new PayrollActionResponse());

        ResponseEntity<?> response = controller.processProvision(id, new PayrollActionRequest(), "Y");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void processProvision_DefaultPostJv_Success() {
        Long id = 1L;

        when(hrPayrollProcessService.processProvision(eq(id), any(PayrollActionRequest.class), isNull())).thenReturn(new PayrollActionResponse());

        // This would be how it's called from HTTP request without postJv parameter
        ResponseEntity<?> response = controller.processProvision(id, new PayrollActionRequest(), null);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void revertPayroll_Success() {
        Long id = 1L;

        PayrollActionResponse payrollActionResponse = new PayrollActionResponse();
        payrollActionResponse.setStatus("SUCCESS");

        when(hrPayrollProcessService.revertPayroll(id)).thenReturn(payrollActionResponse);

        ResponseEntity<?> response = controller.revertPayroll(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void revertPayroll_Failure() {
        Long id = 1L;

        PayrollActionResponse payrollActionResponse = new PayrollActionResponse();
        payrollActionResponse.setStatus("FAILURE");
        payrollActionResponse.setMessage("Payroll revert failed");

        when(hrPayrollProcessService.revertPayroll(id)).thenReturn(payrollActionResponse);

        ResponseEntity<?> response = controller.revertPayroll(id);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void loadVariables_Success() {
        Long id = 1L;
        LoadVariablesRequest request = new LoadVariablesRequest();
        request.setSettlementPoid(2L);
        request.setEmpPoid(100L);
        request.setPayrollDate("2024-01-31");

        when(hrPayrollProcessService.loadVariables(eq(id), eq(2L), eq(100L), eq("2024-01-31"))).thenReturn(new VariableLoadResponse());

        ResponseEntity<?> response = controller.loadVariables(id, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void loadLoansAdvances_Success() {
        Long id = 1L;
        LoadLoansAdvancesRequest request = new LoadLoansAdvancesRequest();
        request.setSettlementPoid(2L);
        request.setEmpPoid(100L);
        request.setPayrollDate(LocalDate.of(2024, 1, 31));

        when(hrPayrollProcessService.loadLoansAdvances(eq(id), eq(2L), eq(100L), eq(LocalDate.of(2024, 1, 31)))).thenReturn(new LoansAdvancesResponse());

        ResponseEntity<?> response = controller.loadLoansAdvances(id, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void createJv_Success() {
        Long id = 1L;
        
        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getUserPoid).thenReturn(5L);
            
            when(hrPayrollProcessService.createJv(eq(5L), eq(id), eq("BANK"))).thenReturn(new JvCreationResponse());

            ResponseEntity<?> response = controller.createJv(id, "BANK");

            assertEquals(200, response.getStatusCode().value());
        }
    }

    @Test
    void generateBankFile_Success() {
        Long id = 1L;
        when(hrPayrollProcessService.generateBankFile(id)).thenReturn(new BankFileResponse());

        ResponseEntity<?> response = controller.generateBankFile(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void hsbcApiTransfer_Success() {
        Long id = 1L;
        when(hrPayrollProcessService.hsbcApiTransfer(id)).thenReturn(new BankFileResponse());

        ResponseEntity<?> response = controller.hsbcApiTransfer(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void syncHrData_Success() {
        when(hrPayrollProcessService.syncHrData(any())).thenReturn(new PayrollActionResponse());

        ResponseEntity<?> response = controller.syncHrData(1L);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void sendEmail_WithRequest_Success() {
        Long id = 1L;
        when(hrPayrollProcessService.sendEmail(eq(id), any())).thenReturn(new PayrollActionResponse());

        ResponseEntity<?> response = controller.sendEmail(id, new PayrollActionRequest());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void sendEmail_NullRequest_DefaultsToEmptyRequest() {
        Long id = 1L;
        when(hrPayrollProcessService.sendEmail(eq(id), any())).thenReturn(new PayrollActionResponse());

        ResponseEntity<?> response = controller.sendEmail(id, null);

        assertEquals(200, response.getStatusCode().value());
        verify(hrPayrollProcessService).sendEmail(eq(id), any(PayrollActionRequest.class));
    }

    // ── Print endpoints ───────────────────────────────────────────────────────

    @Test
    void printPayslip_Success() throws Exception {
        Long id = 1L;
        when(hrPayrollProcessService.printPayslip(id)).thenReturn("PDF".getBytes());

        ResponseEntity<?> response = controller.printPayslip(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void printPayslip_Exception_ReturnsError() throws Exception {
        Long id = 1L;
        when(hrPayrollProcessService.printPayslip(id)).thenThrow(new RuntimeException("PDF generation failed"));

        ResponseEntity<?> response = controller.printPayslip(id);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void printPreview_Success() throws Exception {
        Long id = 1L;
        when(hrPayrollProcessService.printPreview(id)).thenReturn("PDF".getBytes());

        ResponseEntity<?> response = controller.printPreview(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void printPreview_Exception_ReturnsError() throws Exception {
        Long id = 1L;
        when(hrPayrollProcessService.printPreview(id)).thenThrow(new RuntimeException("PDF generation failed"));

        ResponseEntity<?> response = controller.printPreview(id);

        assertEquals(500, response.getStatusCode().value());
    }
}
