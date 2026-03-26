package com.asg.payroll.loansadvances.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductRequest;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductResponse;
import com.asg.payroll.loansadvances.service.LoansAdvancesService;
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
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoansAdvancesControllerTest {

    @Mock
    private LoggingService loggingService;

    @Mock
    private LoansAdvancesService service;

    @InjectMocks
    private LoansAdvancesController controller;

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
    void testCreateLoans() {
        HrRecurringPayDeductRequest request = new HrRecurringPayDeductRequest();
        when(service.create(any(HrRecurringPayDeductRequest.class))).thenReturn(100L);

        ResponseEntity<?> responseEntity = controller.createLoans(request);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        verify(service, times(1)).create(request);
    }

    @Test
    void testUpdateLoans() {
        HrRecurringPayDeductRequest request = new HrRecurringPayDeductRequest();
        HrRecurringPayDeductResponse responseDto = HrRecurringPayDeductResponse.builder().transactionPoid(100L).build();
        when(service.update(eq(100L), any(HrRecurringPayDeductRequest.class))).thenReturn(responseDto);

        ResponseEntity<?> responseEntity = controller.updateLoans(100L, request);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        verify(service, times(1)).update(100L, request);
    }

    @Test
    void testGetLoanById() {
        HrRecurringPayDeductResponse responseDto = HrRecurringPayDeductResponse.builder().transactionPoid(100L).build();
        when(service.getById(100L)).thenReturn(responseDto);

        ResponseEntity<?> responseEntity = controller.getLoanById(100L);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        verify(service, times(1)).getById(100L);
        verify(loggingService, times(1)).createLogSummaryEntry(eq(LogDetailsEnum.VIEWED), eq("DOC-123"), eq("100"));
    }

    @Test
    void testListLoans() {
        Pageable pageable = PageRequest.of(0, 10);
        FilterRequestDto filterRequest = new FilterRequestDto("AND", "N", List.of());
        Map<String, Object> mockResult = Map.of("data", "test");
        when(service.list(any(), any())).thenReturn(mockResult);

        ResponseEntity<?> responseEntity = controller.listLoans(pageable, filterRequest);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        verify(service, times(1)).list(filterRequest, pageable);
    }

    @Test
    void testDeleteLoans() {
        DeleteReasonDto deleteReason = new DeleteReasonDto();
        doNothing().when(service).delete(eq(100L), any());

        ResponseEntity<?> responseEntity = controller.deleteLoans(100L, deleteReason);

        assertNotNull(responseEntity);
        assertEquals(200, responseEntity.getStatusCode().value());
        verify(service, times(1)).delete(100L, deleteReason);
    }
}