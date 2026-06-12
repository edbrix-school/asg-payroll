package com.asg.payroll.loansadvances.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class HrRecurringPayDeductResponse {

    private Long transactionPoid;
    private Long groupPoid;
    private String companyPoid;
    private LocalDate transactionDate;
    private String docRef;

//    private String recurType;
//    private String glCode;

    private Long employeePoid;
    private String refNo;
    private String descriptions;

    private BigDecimal totalAmount;
    private BigDecimal openingAmount;
    private LocalDate startDate;
    private BigDecimal monthlyAmount;

    private String sourceDocId;
    private Long sourceDocPoid;
    private String sourceDocRef;

    private String settledAndClosed;
    private String receiptNo;
    private LocalDate receiptDate;

}
