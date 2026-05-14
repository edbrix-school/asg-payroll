package com.asg.payroll.payrollprocess.dto;

import com.asg.payroll.common.util.ActionType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollRecurringDtlRequest {

    private Long detRowId;
    private Long employeePoid;
    private String refNo;
    private BigDecimal balAmt;
    private BigDecimal recurAmount;
    private String remarks;
    private ActionType actionType;
}
