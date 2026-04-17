package com.asg.payroll.employeeappraisal.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "HR_APPRAISAL_HDR")
@Getter
@Setter
public class HrAppraisalHdr extends BaseEntity {

    @Id
    @AuditIgnore
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @AuditIgnore
    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @AuditIgnore
    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @AuditIgnore
    @Column(name = "DOC_REF", length = 30, unique = true)
    private String docRef;

    @Column(name = "PERIOD_FROM")
    private LocalDate periodFrom;

    @Column(name = "PERIOD_TO")
    private LocalDate periodTo;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "VERIFIED_BY", length = 20)
    private String verifiedBy;

    @Column(name = "APPROVED_BY", length = 20)
    private String approvedBy;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "COMPLETED", length = 1)
    private String completed;

    @Column(name = "COMPLETED_ON")
    private LocalDateTime completedOn;

    @Column(name = "GRID_LISTING_METHOD", length = 50)
    private String gridListingMethod;

    @Column(name = "TOTAL_ARREARS")
    private BigDecimal totalArrears;

    @Column(name = "JV_DOC_REF", length = 50)
    private String jvDocRef;

    @Column(name = "JV_DOC_POID", length = 50)
    private String jvDocPoid;

    @Column(name = "ARREARS_PAYROLL_POID")
    private Long arrearsPayrollPoid;

    @Column(name = "LETTER_EMAILED_ON")
    private LocalDate letterEmailedOn;

    @Column(name = "APPRAISAL_BASIC_PERCENT")
    private BigDecimal appraisalBasicPercent;

    @Column(name = "APPRAISAL_FIXED_PERCENT")
    private BigDecimal appraisalFixedPercent;
}