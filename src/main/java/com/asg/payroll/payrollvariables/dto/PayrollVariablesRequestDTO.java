package com.asg.payroll.payrollvariables.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollVariablesRequestDTO {

    @NotNull(message = "Transaction date is mandatory")
    private LocalDate transactionDate;

    @NotNull(message = "Employee POID is mandatory")
    private Long employeePoid;

    @NotNull(message = "Enter Amount...")
    private BigDecimal amount;

    @NotNull(message = "Select Payroll Month...")
    private LocalDate payrollMonth;

    @NotBlank(message = "Remarks is required...")
    @Size(max = 200, message = "Remarks must be at most 200 characters")
    private String remarks;
}
