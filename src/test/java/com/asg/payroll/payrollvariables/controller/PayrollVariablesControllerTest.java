package com.asg.payroll.payrollvariables.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.exceptions.GlobalExceptionHandler;
import com.asg.payroll.payrollvariables.dto.PayrollVariablesRequestDTO;
import com.asg.payroll.payrollvariables.dto.PayrollVariablesResponseDTO;
import com.asg.payroll.payrollvariables.service.PayrollVariablesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PayrollVariablesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PayrollVariablesService service;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private PayrollVariablesController controller;

    private ObjectMapper objectMapper;
    private PayrollVariablesRequestDTO requestDTO;
    private PayrollVariablesResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        requestDTO = PayrollVariablesRequestDTO.builder()
                .transactionDate(LocalDate.now())
                .employeePoid(10L)
                .amount(BigDecimal.valueOf(500))
                .payrollMonth(LocalDate.of(2024, 1, 1))
                .remarks("Test remark")
                .build();

        responseDTO = PayrollVariablesResponseDTO.builder()
                .transactionPoid(1L)
                .companyPoid(1L)
                .groupPoid(1L)
                .transactionDate(LocalDate.now())
                .docRef("VAR-001")
                .employeePoid(10L)
                .amount(BigDecimal.valueOf(500))
                .payrollMonth(LocalDate.of(2024, 1, 1))
                .remarks("Test remark")
                .build();
    }

    @Test
    void create_Success() throws Exception {
        when(service.create(any(PayrollVariablesRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/v1/payroll-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.transactionPoid").value(1L))
                .andExpect(jsonPath("$.result.data.employeePoid").value(10L));

        verify(service).create(any(PayrollVariablesRequestDTO.class));
    }

    @Test
    void create_ValidationError_MissingRequiredFields() throws Exception {
        PayrollVariablesRequestDTO invalid = PayrollVariablesRequestDTO.builder().build();

        mockMvc.perform(post("/v1/payroll-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ValidationError_NegativeAmount_IsAllowed() throws Exception {
        requestDTO.setAmount(BigDecimal.valueOf(-100));
        when(service.create(any(PayrollVariablesRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/v1/payroll-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void create_ValidationError_NullAmount() throws Exception {
        requestDTO.setAmount(null);

        mockMvc.perform(post("/v1/payroll-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ValidationError_BlankRemarks() throws Exception {
        requestDTO.setRemarks("");

        mockMvc.perform(post("/v1/payroll-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ValidationError_NullRemarks() throws Exception {
        requestDTO.setRemarks(null);

        mockMvc.perform(post("/v1/payroll-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ServiceException() throws Exception {
        when(service.create(any())).thenThrow(new ValidationException("DB error"));

        mockMvc.perform(post("/v1/payroll-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_InvalidJson() throws Exception {
        mockMvc.perform(post("/v1/payroll-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_Success() throws Exception {
        when(service.update(eq(1L), any(PayrollVariablesRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/v1/payroll-variables/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.transactionPoid").value(1L));

        verify(service).update(eq(1L), any(PayrollVariablesRequestDTO.class));
    }

    @Test
    void update_NotFound() throws Exception {
        when(service.update(eq(99L), any())).thenThrow(new ResourceNotFoundException("Not found", "id", 99L));

        mockMvc.perform(put("/v1/payroll-variables/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_Success() throws Exception {
        when(service.getById(1L)).thenReturn(responseDTO);

        try (MockedStatic<UserContext> mockedCtx = mockStatic(UserContext.class)) {
            mockedCtx.when(UserContext::getDocumentId).thenReturn("DOC123");

            mockMvc.perform(get("/v1/payroll-variables/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data.transactionPoid").value(1L));

            verify(loggingService).createLogSummaryEntry(any(LogDetailsEnum.class), eq("DOC123"), eq("1"));
        }
    }

    @Test
    void getById_NotFound() throws Exception {
        when(service.getById(99L)).thenThrow(new ResourceNotFoundException("Not found", "id", 99L));

        mockMvc.perform(get("/v1/payroll-variables/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_InvalidId() throws Exception {
        mockMvc.perform(get("/v1/payroll-variables/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void softDelete_Success() throws Exception {
        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();
        deleteReasonDto.setDeleteReason("No longer needed");

        doNothing().when(service).softDelete(eq(1L), any(DeleteReasonDto.class));

        mockMvc.perform(delete("/v1/payroll-variables/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteReasonDto)))
                .andExpect(status().isOk());

        verify(service).softDelete(eq(1L), any(DeleteReasonDto.class));
    }

    @Test
    void softDelete_WithoutBody() throws Exception {
        doNothing().when(service).softDelete(eq(1L), isNull());

        mockMvc.perform(delete("/v1/payroll-variables/1"))
                .andExpect(status().isOk());

        verify(service).softDelete(eq(1L), isNull());
    }

    @Test
    void softDelete_ServiceException() throws Exception {
        doThrow(new ValidationException("Cannot delete")).when(service).softDelete(eq(1L), any());

        mockMvc.perform(delete("/v1/payroll-variables/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_Success() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("content", List.of(responseDTO));
        data.put("totalElements", 1);

        when(service.list(anyString(), any(), any(Pageable.class), any(), any())).thenReturn(data);

        try (MockedStatic<UserContext> mockedCtx = mockStatic(UserContext.class)) {
            mockedCtx.when(UserContext::getDocumentId).thenReturn("DOC123");

            mockMvc.perform(post("/v1/payroll-variables/list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data.totalElements").value(1));

            verify(service).list(eq("DOC123"), any(), any(Pageable.class), isNull(), isNull());
        }
    }

    @Test
    void list_WithDateRange() throws Exception {
        Map<String, Object> data = new HashMap<>();
        when(service.list(anyString(), any(), any(Pageable.class), any(LocalDate.class), any(LocalDate.class))).thenReturn(data);

        try (MockedStatic<UserContext> mockedCtx = mockStatic(UserContext.class)) {
            mockedCtx.when(UserContext::getDocumentId).thenReturn("DOC123");

            mockMvc.perform(post("/v1/payroll-variables/list")
                            .param("periodFrom", "2024-01-01")
                            .param("periodTo", "2024-12-31")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void list_WithFilters() throws Exception {
        Map<String, Object> data = new HashMap<>();
        FilterRequestDto filters = new FilterRequestDto("AND", "N", List.of());
        when(service.list(anyString(), any(FilterRequestDto.class), any(Pageable.class), any(), any())).thenReturn(data);

        try (MockedStatic<UserContext> mockedCtx = mockStatic(UserContext.class)) {
            mockedCtx.when(UserContext::getDocumentId).thenReturn("DOC123");

            mockMvc.perform(post("/v1/payroll-variables/list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(filters)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void list_ServiceException() throws Exception {
        when(service.list(anyString(), any(), any(Pageable.class), any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        try (MockedStatic<UserContext> mockedCtx = mockStatic(UserContext.class)) {
            mockedCtx.when(UserContext::getDocumentId).thenReturn("DOC123");

            mockMvc.perform(post("/v1/payroll-variables/list")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isInternalServerError());
        }
    }
}
