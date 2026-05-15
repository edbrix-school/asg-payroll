package com.asg.payroll.payrollvariables.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.payroll.payrollvariables.dto.PayrollVariablesRequestDTO;
import com.asg.payroll.payrollvariables.dto.PayrollVariablesResponseDTO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface PayrollVariablesService {
    PayrollVariablesResponseDTO create(PayrollVariablesRequestDTO request);
    PayrollVariablesResponseDTO update(Long transactionPoid, PayrollVariablesRequestDTO request);
    PayrollVariablesResponseDTO getById(Long transactionPoid);
    void softDelete(Long transactionPoid, DeleteReasonDto deleteReasonDto);
    Map<String, Object> list(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);
}
