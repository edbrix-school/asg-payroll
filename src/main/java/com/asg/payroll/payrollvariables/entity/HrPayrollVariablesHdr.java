package com.asg.payroll.payrollvariables.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "HR_PAYROLL_VARIABLES_HDR")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollVariablesHdr extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @Column(name = "DOC_REF", length = 25)
    private String docRef;

    @Column(name = "EMPLOYEE_POID", nullable = false)
    private Long employeePoid;

    @Column(name = "ALLOWANCE_DEDUCTION_POID")
    private Long allowanceDeductionPoid;

    @Column(name = "AMOUNT", precision = 13, scale = 3)
    private BigDecimal amount;

    @Column(name = "PAYROLL_MONTH")
    private LocalDate payrollMonth;

    @Column(name = "REMARKS", length = 200)
    private String remarks;

    @Column(name = "DELETED", length = 1)
    private String deleted = "N";
}
