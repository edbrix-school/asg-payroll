package com.asg.payroll.payrollprocess.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "HR_PAYROLL_RECURRING_DTL")
@IdClass(HrPayrollRecurringDtlId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollRecurringDtl extends BaseEntity {

    @Id
    @AuditIgnore
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Id
    @AuditIgnore
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

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

    @Column(name = "REMARKS", length = 200)
    private String remarks;

    @Column(name = "RECUR_TRAN_POID")
    private Long recurTranPoid;

    @Column(name = "ALLOWANCE_DEDUCTION_POID")
    private Long allowanceDeductionPoid;

    @Column(name = "BAL_AMT")
    private BigDecimal balAmt;
}
