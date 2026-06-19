package com.asg.payroll.payrollprocess.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollRecurringDtlResponse {

    private Long detRowId;
    private Long employeePoid;
    private String employeeName;
    private String refNo;
    private BigDecimal balAmt;
    private BigDecimal recurAmount;
    private String remarks;
}