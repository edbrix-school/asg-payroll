package com.asg.payroll.employeeappraisal.dto;

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
    private String description;
    private String verifiedBy;
    private String approvedBy;
    private String completed;
    private LocalDateTime completedOn;
    private String gridListingMethod;
    private BigDecimal totalArrears;
    private String jvDocRef;
    private String jvDocPoid;
    private Long arrearsPayrollPoid;
    private LocalDate letterEmailedOn;
    private BigDecimal appraisalBasicPercent;
    private BigDecimal appraisalFixedPercent;
    private List<HrAppraisalDtlRequest> details;
}
