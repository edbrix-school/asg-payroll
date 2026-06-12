package com.asg.payroll.payrollprocess.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "HR_PAYROLL_DTL")
@IdClass(HrPayrollDtlId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollDtl extends BaseEntity {

    @Id
    @AuditIgnore
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Id
    @AuditIgnore
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "EMPLOYEE_POID")
    private Long employeePoid;

    @AuditIgnore
    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "BASIC_SALARY", precision = 13, scale = 3)
    private BigDecimal basicSalary;

    @Column(name = "BASIC_SALARY_PAYABLE", precision = 13, scale = 3)
    private BigDecimal basicSalaryPayable;

    @Column(name = "WORKED_DAYS")
    private BigDecimal workedDays;

    @Column(name = "ABSENT_DAYS")
    private BigDecimal absentDays;

    @Column(name = "ABSENT_AMOUNT")
    private BigDecimal absentAmount;

    @Column(name = "OT1_HRS")
    private BigDecimal ot1Hrs;

    @Column(name = "OT1_AMT")
    private BigDecimal ot1Amt;

    @Column(name = "OT2_HRS")
    private BigDecimal ot2Hrs;

    @Column(name = "OT2_AMT")
    private BigDecimal ot2Amt;

    @Column(name = "SHORT_HRS")
    private BigDecimal shortHrs;

    @Column(name = "SHORT_HRS_AMOUNT")
    private BigDecimal shortHrsAmount;

    @Column(name = "MEDICAL_DAYS")
    private BigDecimal medicalDays;

    @Column(name = "FIXED_ALLOWANCE")
    private BigDecimal fixedAllowance;

    @Column(name = "HRA_ALLOWANCE")
    private BigDecimal hraAllowance;

    @Column(name = "TRAVEL_ALLOWANCE")
    private BigDecimal travelAllowance;

    @Column(name = "COMMUNICATION_ALLOWANCE")
    private BigDecimal communicationAllowance;

    @Column(name = "TRANSPORT_ALLOWANCE")
    private BigDecimal transportAllowance;

    @Column(name = "SPECIAL_ALLOWANCE")
    private BigDecimal specialAllowance;

    @Column(name = "GOSI_DEDUCTION")
    private BigDecimal gosiDeduction;

    @Column(name = "LOAN_DEDUCTION")
    private BigDecimal loanDeduction;

    @Column(name = "PHONE_DEDUCTION")
    private BigDecimal phoneDeduction;

    @Column(name = "MEDICAL_INSURANCE")
    private BigDecimal medicalInsurance;

    @Column(name = "ARREARS")
    private BigDecimal arrears;

    @Column(name = "RECURRING_DEDUCTION")
    private BigDecimal recurringDeduction;

    @Column(name = "GROSS_SALARY")
    private BigDecimal grossSalary;

    @Column(name = "TOT_DEDUCTIONS")
    private BigDecimal totDeductions;

    @Column(name = "NET_SALARY")
    private BigDecimal netSalary;

    @Column(name = "BANK_POID")
    private Long bankPoid;

    @Column(name = "ACCOUNT_NO", length = 30)
    private String accountNo;

    @Column(name = "HOLD_SALARY", length = 1)
    private String holdSalary;

    @Column(name = "HOLD_REASON", length = 100)
    private String holdReason;

    @Column(name = "REMARKS", length = 100)
    private String remarks;

    @Column(name = "DEPT_POID")
    private Long deptPoid;

    @Column(name = "DESIG_POID")
    private Long desigPoid;

    @Column(name = "PAYMENT_METHOD", length = 30)
    private String paymentMethod;

    @Column(name = "REGISTERED_SALARY")
    private BigDecimal registeredSalary;

    @Column(name = "CR_POID")
    private Long crPoid;

    @Column(name = "LEAVE_DAYS")
    private BigDecimal leaveDays;

    @Column(name = "TOT_ALLOWANCE")
    private BigDecimal totAllowance;

    @Column(name = "PAYSLIP_EMAILED_ON")
    private LocalDateTime payslipEmailedOn;

    @Column(name = "PAYSLIP_PRINTED_ON")
    private LocalDateTime payslipPrintedOn;

    @Column(name = "FIXED_OT")
    private BigDecimal fixedOt;
}
