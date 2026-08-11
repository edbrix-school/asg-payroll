package com.asg.payroll.loansadvances.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductRequest;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface LoansAdvancesService {
    Long create(HrRecurringPayDeductRequest request);

    HrRecurringPayDeductResponse getById(Long id);

    HrRecurringPayDeductResponse update(Long id, HrRecurringPayDeductRequest request);

    void delete(Long id, DeleteReasonDto deleteReasonDto);

    Map<String, Object> list(FilterRequestDto filterRequest, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);
}
