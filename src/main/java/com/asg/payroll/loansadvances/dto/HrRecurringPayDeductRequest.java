package com.asg.payroll.loansadvances.dto;

import jakarta.validation.constraints.Digits;
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

    @Digits(integer = 10, fraction = 3, message = "Total Amount exceeds the maximum allowed limit")
    private BigDecimal totalAmount;

    @NotNull(message = "Opening Amount is mandatory")
    @Digits(integer = 10, fraction = 3, message = "Opening Amount exceeds the maximum allowed limit")
    private BigDecimal openingAmount;

    @NotNull(message = "Start Date is mandatory")
    private LocalDate startDate;

    @NotNull(message = "Monthly Amount is mandatory")
    @Digits(integer = 10, fraction = 3, message = "Monthly Amount exceeds the maximum allowed limit")
    private BigDecimal monthlyAmount;

    private String settledAndClosed;
    private String receiptNo;
    private LocalDate receiptDate;

}
