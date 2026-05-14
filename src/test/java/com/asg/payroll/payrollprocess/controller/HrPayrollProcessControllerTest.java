package com.asg.payroll.payrollprocess.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.payrollprocess.dto.HrPayrollHdrRequest;
import com.asg.payroll.payrollprocess.dto.PayrollActionRequest;
import com.asg.payroll.payrollprocess.service.HrPayrollProcessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrPayrollProcessControllerTest {

    @Mock
    private HrPayrollProcessService hrPayrollProcessService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private HrPayrollProcessController controller;

    private Map<String, Object> emptyResponse() {
        return new HashMap<>();
    }

    @Test
    void listPayrolls_Success() {
        when(hrPayrollProcessService.listPayrolls(any(), any(), any(), any(), any())).thenReturn(emptyResponse());

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
        when(hrPayrollProcessService.getPayrollById(id)).thenReturn(emptyResponse());

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

        when(hrPayrollProcessService.createPayroll(any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.createPayroll(request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updatePayroll_Success() {
        Long id = 1L;
        HrPayrollHdrRequest request = new HrPayrollHdrRequest();
        request.setAttendTranPoid(10L);
        request.setPayrollMonth(LocalDate.of(2024, 1, 31));

        when(hrPayrollProcessService.updatePayroll(eq(id), any())).thenReturn(emptyResponse());

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

        when(hrPayrollProcessService.processPayroll(eq(id), any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.processPayroll(id, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void processProvision_Success() {
        Long id = 1L;

        when(hrPayrollProcessService.processProvision(eq(id), any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.processProvision(id, new PayrollActionRequest());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void revertPayroll_Success() {
        Long id = 1L;
        when(hrPayrollProcessService.revertPayroll(id)).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.revertPayroll(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void loadVariables_Success() {
        Long id = 1L;

        when(hrPayrollProcessService.loadVariables(eq(id), any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.loadVariables(id, new PayrollActionRequest());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void loadLoansAdvances_Success() {
        Long id = 1L;

        when(hrPayrollProcessService.loadLoansAdvances(eq(id), any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.loadLoansAdvances(id, new PayrollActionRequest());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void createJv_Success() {
        Long id = 1L;
        when(hrPayrollProcessService.createJv(id)).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.createJv(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void generateBankFile_Success() {
        Long id = 1L;
        when(hrPayrollProcessService.generateBankFile(id)).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.generateBankFile(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void hsbcApiTransfer_Success() {
        Long id = 1L;
        when(hrPayrollProcessService.hsbcApiTransfer(id)).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.hsbcApiTransfer(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void syncHrData_Success() {
        when(hrPayrollProcessService.syncHrData()).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.syncHrData();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void sendEmail_WithRequest_Success() {
        Long id = 1L;
        when(hrPayrollProcessService.sendEmail(eq(id), any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.sendEmail(id, new PayrollActionRequest());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void sendEmail_NullRequest_DefaultsToEmptyRequest() {
        Long id = 1L;
        when(hrPayrollProcessService.sendEmail(eq(id), any())).thenReturn(emptyResponse());

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
