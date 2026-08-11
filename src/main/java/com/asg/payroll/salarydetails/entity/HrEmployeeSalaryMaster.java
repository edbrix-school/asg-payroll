package com.asg.payroll.salarydetails.entity;


import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "HR_EMPLOYEE_SALARY_MASTER")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrEmployeeSalaryMaster extends BaseEntity{

    @Id
    @Column(name = "SALARY_POID")
    private Long salaryPoid;

    @Column(name = "EMPLOYEE_POID", unique = true, nullable = false)
    private Long employeePoid;

    @Column(name = "BASIC_SALARY")
    private BigDecimal basicSalary;

    @Column(name = "REGISTERED_SALARY")
    private BigDecimal registeredSalary;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Integer seqno;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "LAST_INCREMENT_DATE")
    private LocalDate lastIncrementDate;

    @Column(name = "NEXT_INCREMENT_DATE")
    private LocalDate nextIncrementDate;

    @Column(name = "GROSS_SALARY")
    private BigDecimal grossSalary;

    @Column(name = "NET_SALARY")
    private BigDecimal netSalary;

    @Column(name = "TOT_ALLOWANCE")
    private BigDecimal totAllowance;

    @Column(name = "TOT_DEDUCTION")
    private BigDecimal totDeduction;

    @Column(name = "PAYMENT_METHOD", length = 20)
    private String paymentMethod;

    @Column(name = "BANK_POID")
    private Long bankPoid;

    @Column(name = "HOLD_SALARY", length = 1)
    private String holdSalary;

    @Column(name = "HOLD_REASON", length = 200)
    private String holdReason;

    @Column(name = "BANK_REGISTRATION_ID", length = 20)
    private String bankRegistrationId;

    @Column(name = "INDEMNITY_DUE_FROM")
    private LocalDate indemnityDueFrom;

    @Column(name = "IBAN_ACCOUNT_NO", length = 30)
    private String ibanAccountNo;

    @Column(name = "BANK_GUARANTEE", length = 1)
    private String bankGuarantee;

    @Column(name = "BANK_GUARANTEE_DETAILS", length = 200)
    private String bankGuaranteeDetails;

    @Column(name = "BANK_GUARANTEE_DUE_DATE")
    private LocalDate bankGuaranteeDueDate;

    @Column(name = "CONTRACT_PRINT_TYPE", length = 20)
    private String contractPrintType;

    @Column(name = "GOSI_TYPE", length = 50)
    private String gosiType;

    @Column(name = "ACCOUNT_NAME", length = 100)
    private String accountName;

    @Column(name = "LOAN_DEDUCTION_AMT")
    private BigDecimal loanDeductionAmt;

    @Column(name = "INDEMNITY_PAID_AMT")
    private BigDecimal indemnityPaidAmt;

    @Column(name = "INDEMNITY_PAID_DET", length = 200)
    private String indemnityPaidDet;

    @Column(name = "ACCOMMODATION_COST")
    private BigDecimal accommodationCost;
}
