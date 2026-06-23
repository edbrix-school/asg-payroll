package com.asg.payroll.employeeappraisal.dto;

import com.asg.payroll.employeeappraisal.enums.AppraisalLoadActionType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrAppraisalActionRequest {
    private String actionType;
    private AppraisalLoadActionType loadActionType;
    private BigDecimal basicIncrementPercent;
    private BigDecimal bonusPercent;
    private String additionalData;
    private String resend;
    private Long payrollPoid;
}
