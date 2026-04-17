package com.asg.payroll.employeeappraisal.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HrAppraisalActionRequest {
    private String actionType;
    private BigDecimal basicIncrementPercent;
    private BigDecimal bonusPercent;
    private String additionalData;
    private String resend;
    private Long payrollPoid;
}
