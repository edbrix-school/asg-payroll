package com.asg.payroll.employeeappraisal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrAppraisalRecalculationRequest {
    private String mode;
    private String fieldName;
    private LocalDate periodFrom;
    BigDecimal netDiffAmount;
    @NotNull(message = "appraisalBasicPercent is required")
    private BigDecimal appraisalBasicPercent;
    @NotNull(message = "appraisalFixedPercent is required")
    private BigDecimal appraisalFixedPercent;
    private HrAppraisalDtlRequest detail;
}
