package com.asg.payroll.payrollprocess.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "HR_PAYROLL_PROVISION_DTL")
@IdClass(HrPayrollProvisionDtlId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollProvisionDtl extends BaseEntity {

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

    @Column(name = "REGISTERED_SALARY")
    private BigDecimal registeredSalary;

    @Column(name = "WORKED_DAYS")
    private BigDecimal workedDays;

    @Column(name = "LEAVE_SALARY")
    private BigDecimal leaveSalary;

    @Column(name = "INDEMNITY")
    private BigDecimal indemnity;

    @Column(name = "AIR_PASSAGE")
    private BigDecimal airPassage;

    @Column(name = "GOSI")
    private BigDecimal gosi;

    @Column(name = "LMRA")
    private BigDecimal lmra;

    @Column(name = "TOTAL_PROVISION")
    private BigDecimal totalProvision;

    @Column(name = "REMARKS", length = 100)
    private String remarks;

    @Column(name = "DEPT_POID")
    private Long deptPoid;

    @Column(name = "DESIG_POID")
    private Long desigPoid;

    @Column(name = "CR_POID")
    private Long crPoid;
}
