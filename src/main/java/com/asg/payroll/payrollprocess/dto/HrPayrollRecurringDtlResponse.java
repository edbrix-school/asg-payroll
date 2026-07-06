package com.asg.payroll.payrollprocess.dto;

import lombok.*;

import java.math.BigDecimal;

import com.asg.common.lib.dto.LovGetListDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollRecurringDtlResponse {

    private Long detRowId;
    private Long employeePoid;
    private LovGetListDto employeeLov;
    private String employeeName;
    private String refNo;
    private BigDecimal balAmt;
    private BigDecimal recurAmount;
    private String remarks;
}