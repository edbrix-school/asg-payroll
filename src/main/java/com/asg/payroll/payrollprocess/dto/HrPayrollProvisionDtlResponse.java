package com.asg.payroll.payrollprocess.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollProvisionDtlResponse {

    private Long detRowId;
    private Long employeePoid;
    private BigDecimal basicSalary;
    private BigDecimal basicSalaryPayable;
    private BigDecimal workedDays;
    private BigDecimal leaveSalary;
    private BigDecimal airPassage;
    private BigDecimal indemnity;
    private BigDecimal gosi;
    private BigDecimal lmra;
    private BigDecimal totalProvision;
    private String remarks;
}