package com.asg.payroll.loansadvances.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HrRecurringPayDeductRequest {

    private String docRef;
    private LocalDate transactionDate;

    @NotNull(message = "Employee is mandatory")
    private Long employeePoid;

    @NotBlank(message = "Reference Number is mandatory")
    private String refNo;

    private String recurType;
    private String glCode;

    private String descriptions;

    private BigDecimal totalAmount;

    @NotNull(message = "Opening Amount is mandatory")
    private BigDecimal openingAmount;

    @NotNull(message = "Start Date is mandatory")
    private LocalDate startDate;

    @NotNull(message = "Monthly Amount is mandatory")
    private BigDecimal monthlyAmount;

    private String settledAndClosed;
    private String receiptNo;
    private LocalDate receiptDate;

}
