package com.asg.payroll.employeeappraisal.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDownloadHeaderService;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalActionRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRecalculationRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRequest;
import com.asg.payroll.employeeappraisal.service.HrAppraisalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrAppraisalControllerTest {

    @Mock
    private HrAppraisalService hrAppraisalService;

    @Mock
    private LoggingService loggingService;

    @Spy

    private DocumentDownloadHeaderService downloadHeaderService =

            new DocumentDownloadHeaderService(mock(JdbcTemplate.class));


    @InjectMocks
    private HrAppraisalController controller;

    private Map<String, Object> emptyResponse() {
        return new HashMap<>();
    }

    @Test
    void listAppraisals_Success() {
        when(hrAppraisalService.listAppraisals(any(), any(), any(), any(), any())).thenReturn(emptyResponse());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            ResponseEntity<?> response = controller.listAppraisals(
                    new FilterRequestDto("AND", "N", List.of()), PageRequest.of(0, 10), null, null);

            assertEquals(200, response.getStatusCode().value());
        }
    }

    @Test
    void getAppraisalById_Success() {
        Long id = 1L;
        when(hrAppraisalService.getAppraisalById(id)).thenReturn(emptyResponse());

        try (MockedStatic<UserContext> ctx = mockStatic(UserContext.class)) {
            ctx.when(UserContext::getDocumentId).thenReturn("DOC123");

            ResponseEntity<?> response = controller.getAppraisalById(id);

            assertEquals(200, response.getStatusCode().value());
            verify(loggingService).createLogSummaryEntry(any(LogDetailsEnum.class), anyString(), anyString());
        }
    }

    @Test
    void createAppraisal_Success() {
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Test Appraisal");
        request.setPeriodFrom(LocalDate.now());
        request.setAppraisalBasicPercent(BigDecimal.TEN);
        request.setAppraisalFixedPercent(BigDecimal.TEN);

        when(hrAppraisalService.createAppraisal(any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.createAppraisal(request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateAppraisal_Success() {
        Long id = 1L;
        HrAppraisalRequest request = new HrAppraisalRequest();
        request.setDescription("Updated Appraisal");

        when(hrAppraisalService.updateAppraisal(eq(id), any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.updateAppraisal(id, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void deleteAppraisal_Success() {
        Long id = 1L;
        doNothing().when(hrAppraisalService).deleteAppraisal(eq(id), any());

        ResponseEntity<?> response = controller.deleteAppraisal(id, new DeleteReasonDto());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getDetailsSp_Success() {
        Long transactionPoid = 1L;
        Long employeePoid = 100L;
        when(hrAppraisalService.getDetailsSp(transactionPoid, employeePoid)).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.getDetailsSp(transactionPoid, employeePoid);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void loadAppraisalData_Success() {
        Long id = 1L;
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setActionType("LOAD_EMPLOYEES_BLANK_DATA");

        when(hrAppraisalService.loadAppraisalDataSp(eq(id), anyString())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.loadAppraisalData(id, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void clearAppraisalData_Success() {
        Long id = 1L;
        when(hrAppraisalService.clearAppraisalDataSp(id)).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.clearAppraisalData(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void batchUpdate_Success() {
        Long id = 1L;
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setBasicIncrementPercent(BigDecimal.valueOf(5));
        request.setBonusPercent(BigDecimal.valueOf(10));

        when(hrAppraisalService.batchUpdateSp(eq(id), any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.batchUpdate(id, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void recalculateDetail_Success() {
        HrAppraisalRecalculationRequest request = new HrAppraisalRecalculationRequest();
        request.setMode("AMOUNT_EDIT");

        when(hrAppraisalService.recalculateDetail(any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.recalculateDetail(request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateData_Success() {
        Long transactionPoid = 1L;
        Long employeePoid = 100L;
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setAdditionalData("test data");

        when(hrAppraisalService.updateDataSp(eq(transactionPoid), eq(employeePoid), any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.updateData(transactionPoid, employeePoid, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateMaster_Success() {
        Long id = 1L;
        when(hrAppraisalService.updateMasterSp(eq(id), any())).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.updateMaster(id, new HrAppraisalActionRequest());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void createJv_Success() {
        Long id = 1L;
        when(hrAppraisalService.createJvSp(id)).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.createJv(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void sendEmail_WithResend_Success() {
        Long id = 1L;
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setResend("Y");

        when(hrAppraisalService.sendEmailSp(id, "Y")).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.sendEmail(id, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void sendEmail_WithoutRequest_DefaultsToN() {
        Long id = 1L;
        when(hrAppraisalService.sendEmailSp(id, "N")).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.sendEmail(id, null);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void bankFile_Success() {
        Long id = 1L;
        when(hrAppraisalService.bankFileSp(id)).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.bankFile(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void arrears_Success() {
        Long id = 1L;
        HrAppraisalActionRequest request = new HrAppraisalActionRequest();
        request.setPayrollPoid(200L);

        when(hrAppraisalService.arrearsSp(id, 200L)).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.arrears(id, request);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void arrearsCalc_Success() {
        Long id = 1L;
        when(hrAppraisalService.arrearsCalcSp(id)).thenReturn(emptyResponse());

        ResponseEntity<?> response = controller.arrearsCalc(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void printA3_Success() throws Exception {
        Long id = 1L;
        when(hrAppraisalService.printA3(id)).thenReturn("PDF".getBytes());

        ResponseEntity<?> response = controller.printA3(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void printA3_Exception_ReturnsError() throws Exception {
        Long id = 1L;
        when(hrAppraisalService.printA3(id)).thenThrow(new RuntimeException("PDF generation failed"));

        ResponseEntity<?> response = controller.printA3(id);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void printByCompany_Success() throws Exception {
        Long id = 1L;
        when(hrAppraisalService.printByCompany(id)).thenReturn("PDF".getBytes());

        ResponseEntity<?> response = controller.printByCompany(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void printByCompany_Exception_ReturnsError() throws Exception {
        Long id = 1L;
        when(hrAppraisalService.printByCompany(id)).thenThrow(new RuntimeException("PDF generation failed"));

        ResponseEntity<?> response = controller.printByCompany(id);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void printBank_Success() throws Exception {
        Long id = 1L;
        when(hrAppraisalService.printBank(id)).thenReturn("PDF".getBytes());

        ResponseEntity<?> response = controller.printBank(id);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void printBank_Exception_ReturnsError() throws Exception {
        Long id = 1L;
        when(hrAppraisalService.printBank(id)).thenThrow(new RuntimeException("PDF generation failed"));

        ResponseEntity<?> response = controller.printBank(id);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void printLetter_WithEmployeePoid_Success() throws Exception {
        Long id = 1L;
        Long employeePoid = 100L;
        when(hrAppraisalService.printLetter(id, employeePoid)).thenReturn("PDF".getBytes());

        ResponseEntity<?> response = controller.printLetter(id, employeePoid);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void printLetter_WithoutEmployeePoid_Success() throws Exception {
        Long id = 1L;
        when(hrAppraisalService.printLetter(id, null)).thenReturn("PDF".getBytes());

        ResponseEntity<?> response = controller.printLetter(id, null);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void printLetter_Exception_ReturnsError() throws Exception {
        Long id = 1L;
        when(hrAppraisalService.printLetter(id, null)).thenThrow(new RuntimeException("PDF generation failed"));

        ResponseEntity<?> response = controller.printLetter(id, null);

        assertEquals(500, response.getStatusCode().value());
    }
}
