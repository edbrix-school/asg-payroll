package com.asg.payroll.employeeappraisal.dto;

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
    private BigDecimal appraisalBasicPercent;
    private BigDecimal appraisalFixedPercent;
    private HrAppraisalDtlRequest detail;
}
