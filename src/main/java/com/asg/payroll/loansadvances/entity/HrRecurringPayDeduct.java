package com.asg.payroll.loansadvances.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "HR_RECURRING_PAY_DEDUCT")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrRecurringPayDeduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "COMPANY_POID", length = 20)
    private String companyPoid;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @Column(name = "DOC_REF", length = 25, nullable = false)
    private String docRef;

    @Column(name = "RECUR_TYPE", length = 20) // we will get using EMP_ALOW_DEDU_TYPE LOV
    private String recurType;

    @Column(name = "GLCODE", length = 20)
    private String glCode;

    @Column(name = "DESCRIPTIONS", length = 200)
    private String descriptions;

    @Column(name = "TOTAL_AMOUNT", nullable = false, precision = 13, scale = 3)
    private BigDecimal totalAmount;

    @Column(name = "OPENING_AMOUNT", nullable = false, precision = 13, scale = 3)
    private BigDecimal openingAmount;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "MONTHLY_AMOUNT", precision = 13, scale = 3)
    private BigDecimal monthlyAmount;

    @Column(name = "EMPLOYEE_POID")
    private Long employeePoid;

    @Column(name = "REF_NO", length = 100, nullable = false)
    private String refNo;

    @Column(name = "SOURCE_DOC_ID", length = 20)
    private String sourceDocId;

    @Column(name = "SOURCE_DOC_POID")
    private Long sourceDocPoid;

    @Column(name = "SOURCE_DOC_REF", length = 50)
    private String sourceDOcRef;

    @Column(name = "SETTLED_AND_CLOSED", length = 10)
    private String settledAndClosed;

    @Column(name = "RECEIPT_NO", length = 20)
    private String receiptNo;

    @Column(name = "RECEIPT_DATE")
    private LocalDate receiptDate;

    @Column(name = "DELETED", length = 1)
    @Builder.Default
    private String deleted = "N";

}
