package com.asg.payroll.salarydetails.dto;

import com.asg.payroll.common.util.ActionType;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryAllowanceDto {

    private Long detRowId;
    private Long allowanceDeductionPoid;
    private String allowanceName;
    private BigDecimal amount;
    private String active;
    private String formula;
    private String remarks;
    private ActionType actionType;
}
