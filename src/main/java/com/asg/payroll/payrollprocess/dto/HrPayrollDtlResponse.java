package com.asg.payroll.payrollprocess.dto;

import lombok.*;

import java.math.BigDecimal;

import com.asg.common.lib.dto.LovGetListDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollDtlResponse {

    private Long detRowId;
    private Long employeePoid;
    private LovGetListDto employeeLov;
    private BigDecimal workedDays;
    private BigDecimal basicSalary;
    private BigDecimal basicSalaryPayable;
    private BigDecimal fixedAllowance;
    private BigDecimal fixedOt;
    private BigDecimal transportAllowance;
    private BigDecimal hraAllowance;
    private BigDecimal grossSalary;
    private BigDecimal loanDeduction;
    private BigDecimal gosiDeduction;
    private BigDecimal totDeductions;
    private BigDecimal netSalary;
    private String accountNo;
    private String holdSalary;
    private String holdReason;
    private String remarks;
}