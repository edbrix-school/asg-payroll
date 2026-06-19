package com.asg.payroll.payrollprocess.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollVarAlwdedDtlResponse {

    private Long detRowId;
    private Long employeePoid;
    private Long allowanceDeductionPoid;
    private BigDecimal amount;
    private String remarks;
}