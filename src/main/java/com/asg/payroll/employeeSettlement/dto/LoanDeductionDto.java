package com.asg.payroll.employeeSettlement.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDeductionDto {

    private Long detRowId;
    private Long transactionPoid;
    private Long employeePoid;
    private String recurType;
    private BigDecimal recurAmount;
    private String refNo;
    private String verified;
    private String remarks;
    private Long recurTranPoid;
    private Long allowanceDeductionPoid;
    private BigDecimal balanceAmt;

}
