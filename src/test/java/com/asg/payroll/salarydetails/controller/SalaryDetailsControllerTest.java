package com.asg.payroll.salarydetails.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.salarydetails.dto.SalaryDetailRequest;
import com.asg.payroll.salarydetails.dto.SalaryDetailResponse;
import com.asg.payroll.salarydetails.service.SalaryDetailsService;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryDetailsControllerTest {

    @Mock
    private LoggingService loggingService;

    @Mock
    private SalaryDetailsService service;

    @InjectMocks
    private SalaryDetailsController controller;

    private MockedStatic<UserContext> userContextMockedStatic;

    @BeforeEach
    void setUp() {
        userContextMockedStatic = Mockito.mockStatic(UserContext.class);
        userContextMockedStatic.when(UserContext::getDocumentId).thenReturn("DOC-123");
    }

    @AfterEach
    void tearDown() {
        if (userContextMockedStatic != null) {
            userContextMockedStatic.close();
        }
    }

    @Test
    void testUpdateSalaryDetail() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        SalaryDetailResponse response = new SalaryDetailResponse();
        when(service.update(1L, request)).thenReturn(response);

        ResponseEntity<?> responseEntity = controller.updateSalaryDetail(1L, request);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(service).update(1L, request);
    }

    @Test
    void testGetSalaryDetailById() {
        SalaryDetailResponse response = new SalaryDetailResponse();
        when(service.getById(1L)).thenReturn(response);

        ResponseEntity<?> responseEntity = controller.getSalaryDetailById(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(service).getById(1L);
        verify(loggingService).createLogSummaryEntry(LogDetailsEnum.VIEWED, "DOC-123", "1");
    }

    @Test
    void testListSalaryDetails() {
        Pageable pageable = PageRequest.of(0, 10);
        FilterRequestDto filterRequest = new FilterRequestDto("AND", "N", List.of());
        Map<String, Object> result = Map.of("data", "test");
        when(service.list(any(), any())).thenReturn(result);

        ResponseEntity<?> responseEntity = controller.listSalaryDetails(pageable, filterRequest);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(service).list(filterRequest, pageable);
    }

    @Test
    void testDeleteSalaryDetail() {
        DeleteReasonDto deleteReason = new DeleteReasonDto();
        doNothing().when(service).delete(1L, deleteReason);

        ResponseEntity<?> responseEntity = controller.deleteSalaryDetail(1L, deleteReason);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(service).delete(1L, deleteReason);
    }

    @Test
    void testAddToHistory_Success() {
        when(service.addToHistory(1L)).thenReturn("SUCCESS: Added to history");

        ResponseEntity<?> responseEntity = controller.addToHistory(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(service).addToHistory(1L);
    }

    @Test
    void testAddToHistory_Failure() {
        when(service.addToHistory(1L)).thenReturn("ERROR: Failed to add");

        ResponseEntity<?> responseEntity = controller.addToHistory(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        verify(service).addToHistory(1L);
    }

    @Test
    void testSyncHRData_Success() {
        when(service.syncHRData()).thenReturn("SUCCESS: Data synced");

        ResponseEntity<?> responseEntity = controller.syncHRData();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(service).syncHRData();
    }

    @Test
    void testSyncHRData_Failure() {
        when(service.syncHRData()).thenReturn("ERROR: Sync failed");

        ResponseEntity<?> responseEntity = controller.syncHRData();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        verify(service).syncHRData();
    }


    @Test
    void testCalculateCTC() {
        when(service.calculateCTC(100L)).thenReturn("5000");

        ResponseEntity<?> responseEntity = controller.calculateCTC(100L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(service).calculateCTC(100L);
    }

    @Test
    void testPrintOfferLetter_Success() throws Exception {
        byte[] pdfContent = new byte[]{1, 2, 3};
        when(service.printOfferLetter(1L)).thenReturn(pdfContent);

        ResponseEntity<?> responseEntity = controller.printOfferLetter(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertArrayEquals(pdfContent, (byte[]) responseEntity.getBody());
    }

    @Test
    void testPrintOfferLetter_Exception() throws Exception {
        when(service.printOfferLetter(1L)).thenThrow(new RuntimeException("Printer error"));

        ResponseEntity<?> responseEntity = controller.printOfferLetter(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void testPrintSalaryCertificate_Success() throws Exception {
        byte[] pdfContent = new byte[]{1, 2, 3};
        when(service.printSalaryCertificate(1L)).thenReturn(pdfContent);

        ResponseEntity<?> responseEntity = controller.printSalaryCertificate(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertArrayEquals(pdfContent, (byte[]) responseEntity.getBody());
    }

    @Test
    void testPrintSalaryCertificate_Exception() throws Exception {
        when(service.printSalaryCertificate(1L)).thenThrow(new RuntimeException("Printer error"));

        ResponseEntity<?> responseEntity = controller.printSalaryCertificate(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void testPrintContract_Success() throws Exception {
        byte[] pdfContent = new byte[]{1, 2, 3};
        when(service.printContract(1L, "OFFICIAL")).thenReturn(pdfContent);

        ResponseEntity<?> responseEntity = controller.printContract(1L, "OFFICIAL");

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertArrayEquals(pdfContent, (byte[]) responseEntity.getBody());
    }

    @Test
    void testPrintContract_Exception() throws Exception {
        when(service.printContract(1L, "OFFICIAL")).thenThrow(new RuntimeException("Printer error"));

        ResponseEntity<?> responseEntity = controller.printContract(1L, "OFFICIAL");

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void testPrintAnnex_Success() throws Exception {
        byte[] pdfContent = new byte[]{1, 2, 3};
        when(service.printContract(1L, null)).thenReturn(pdfContent);

        ResponseEntity<?> responseEntity = controller.printAnnex(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertArrayEquals(pdfContent, (byte[]) responseEntity.getBody());
    }

    @Test
    void testPrintAnnex_Exception() throws Exception {
        when(service.printContract(1L, null)).thenThrow(new RuntimeException("Printer error"));

        ResponseEntity<?> responseEntity = controller.printAnnex(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void testAddToHistory_NullStatus() {
        when(service.addToHistory(1L)).thenReturn(null);
        ResponseEntity<?> responseEntity = controller.addToHistory(1L);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void testSyncHRData_NullStatus() {
        when(service.syncHRData()).thenReturn(null);
        ResponseEntity<?> responseEntity = controller.syncHRData();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}