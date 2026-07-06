package com.asg.payroll.payrollprocess.dto;

import lombok.*;

import java.math.BigDecimal;

import com.asg.common.lib.dto.LovGetListDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollVarAlwdedDtlResponse {

    private Long detRowId;
    private Long employeePoid;
    private LovGetListDto employeeLov;
    private Long allowanceDeductionPoid;
    private LovGetListDto allowanceDeductionLov;
    private BigDecimal amount;
    private String remarks;
}