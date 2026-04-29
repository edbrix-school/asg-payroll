package com.asg.payroll.employeeappraisal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrAppraisalRequest {
    private LocalDate transactionDate;
    private LocalDate periodFrom;
    private LocalDate periodTo;
    @Size(max = 1000, message = "Description must not exceed 1000 characters.")
    private String description;
    @Size(max = 20, message = "Verified by must not exceed 20 characters.")
    private String verifiedBy;
    @Size(max = 20, message = "Approved by must not exceed 20 characters.")
    private String approvedBy;
    private String completed;
    private LocalDateTime completedOn;
    @Size(max = 50, message = "Grid listing method must not exceed 50 characters.")
    private String gridListingMethod;
    private BigDecimal totalArrears;
    private String jvDocRef;
    private String jvDocPoid;
    private Long arrearsPayrollPoid;
    private LocalDate letterEmailedOn;
    @NotNull(message = "appraisalBasicPercent is required")
    private BigDecimal appraisalBasicPercent;
    @NotNull(message = "appraisalFixedPercent is required")
    private BigDecimal appraisalFixedPercent;
    @Valid
    private List<HrAppraisalDtlRequest> details;
}
