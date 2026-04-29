package com.asg.payroll.employeeappraisal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "HR_PAYROLL_VAR_ALWDED_DTL")
@IdClass(HrPayrollVarAlwdedDtlId.class)
@Getter
@Setter
public class HrPayrollVarAlwdedDtl {

    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "EMPLOYEE_POID")
    private Long employeePoid;

    @Column(name = "ALLOWANCE_DEDUCTION_POID")
    private Long allowanceDeductionPoid;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY")
    private String lastmodifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastmodifiedDate;

    @Column(name = "REMARKS", length = 200)
    private String remarks;
}
