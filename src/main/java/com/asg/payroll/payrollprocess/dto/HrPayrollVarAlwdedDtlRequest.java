package com.asg.payroll.payrollprocess.dto;

import com.asg.payroll.common.util.ActionType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollVarAlwdedDtlRequest {

    private Long detRowId;
    private Long employeePoid;
    private Long allowanceDeductionPoid;
    private BigDecimal amount;
    private String remarks;
    private ActionType actionType;
}
