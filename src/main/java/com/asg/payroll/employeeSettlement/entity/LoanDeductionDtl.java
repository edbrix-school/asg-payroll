package com.asg.payroll.employeeSettlement.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name="HR_LEAVE_SETTLEMENT_LOANS_DTL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(LoanDeductionDtlId.class)
public class LoanDeductionDtl extends BaseEntity {
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "EMPLOYEE_POID")
    private Long employeePoid;

    @Column(name = "RECUR_TYPE", length = 20)
    private String recurType;

    @Column(name = "RECUR_AMOUNT", precision = 13, scale = 3)
    private BigDecimal recurAmount;

    @Column(name = "REF_NO", length = 100)
    private String refNo;

    @Column(name = "VERIFIED", length = 1)
    private String verified;

    @Column(name = "REMARKS", length = 2000)
    private String remarks;

    @Column(name = "RECUR_TRAN_POID")
    private Long recurTranPoid;

    @Column(name = "ALLOWANCE_DEDUCTION_POID")
    private Long allowanceDeductionPoid;

    @Column(name = "BALANCE_AMT")
    private BigDecimal balanceAmt;
}
