package com.asg.payroll.payrollprocess.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "HR_PAYROLL_HDR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollHdr extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @AuditIgnore
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @AuditIgnore
    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @AuditIgnore
    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "PAYROLL_MONTH")
    private LocalDate payrollMonth;

    @Column(name = "ATTENDANCE_PERIOD", length = 20)
    private String attendancePeriod;

    @Column(name = "ATTENDANCE_PERIOD_DESC", length = 100)
    private String attendancePeriodDesc;

    @Column(name = "ATTEND_TRAN_POID")
    private Long attendTranPoid;

    @Column(name = "VERIFIED", length = 1)
    private String verified;

    @Column(name = "APPROVED", length = 1)
    private String approved;

    @Column(name = "PAYROLL_RELEASED", length = 1)
    private String payrollReleased;

    @Column(name = "EXPORT_BANKFILE", length = 1)
    private String exportBankfile;

    @Column(name = "PRINT_PAYSLIP", length = 1)
    private String printPayslip;

    @AuditIgnore
    @Column(name = "DOC_REF", length = 25, unique = true)
    private String docRef;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "PAYROLL_JV_DOC_REF", length = 20)
    private String payrollJvDocRef;

    @Column(name = "PAYROLL_JV_DOC_POID", length = 20)
    private String payrollJvDocPoid;

    @Column(name = "PROV_JV_DOC_REF", length = 20)
    private String provJvDocRef;

    @Column(name = "PROV_JV_DOC_POID", length = 20)
    private String provJvDocPoid;

    @Column(name = "BANK_TRANSFER_VALUE_DATE")
    private LocalDate bankTransferValueDate;

    @Column(name = "EMAIL_PAYSLIP", length = 1)
    private String emailPayslip;

    @Column(name = "EMAIL_PAYSLIP_COMPLETED_ON")
    private LocalDateTime emailPayslipCompletedOn;

    @Column(name = "BANK_TRANSFER_DOC_REF", length = 30)
    private String bankTransferDocRef;

    @Column(name = "BANK_TRANSFER_DOC_POID", length = 30)
    private String bankTransferDocPoid;

    @Column(name = "EMAIL_PAYSLIP_SCHEDULE_ON")
    private LocalDateTime emailPayslipScheduleOn;

    @Column(name = "SUPPRESS_ARREARS_VALIDATION", length = 1)
    private String suppressArrearsValidation;

    @Column(name = "TOTAL_NET_SAL")
    private BigDecimal totalNetSal;
}
