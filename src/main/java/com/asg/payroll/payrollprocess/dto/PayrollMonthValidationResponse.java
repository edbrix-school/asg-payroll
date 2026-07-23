package com.asg.payroll.payrollprocess.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollMonthValidationResponse {

    /** False when the payroll month is missing or already used by another payroll. */
    private Boolean valid;

    /** Input date normalised to month end, i.e. the value the UI should post back. */
    private LocalDate payrollMonth;

    /** Reason shown to the user when {@code valid} is false; null when valid. */
    private String message;

    /** DOC_REF of the payroll already holding this month, when one exists. */
    private String existingDocRef;
}
