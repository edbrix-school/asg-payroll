package com.asg.payroll.salarydetails.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.payroll.salarydetails.dto.SalaryDetailRequest;
import com.asg.payroll.salarydetails.dto.SalaryDetailResponse;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface SalaryDetailsService {

    SalaryDetailResponse update(Long id, SalaryDetailRequest request);

    SalaryDetailResponse getById(Long id);

    void delete(Long id, DeleteReasonDto deleteReasonDto);

    Map<String, Object> list(FilterRequestDto filterRequest, Pageable pageable);

    String addToHistory(Long salaryPoid);

    String syncHRData();

    String calculateCTC(Long employeePoid);

    byte[] printOfferLetter(Long id) throws Exception;

    byte[] printSalaryCertificate(Long id) throws Exception;

    byte[] printContract(Long id, String contractPrintType) throws Exception;

}
