package com.asg.payroll.payrollvariables.dto;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollVariablesResponseDTO {

    private Long transactionPoid;
    private Long companyPoid;
    private Long groupPoid;
    private LocalDate transactionDate;
    private String docRef;
    private Long employeePoid;
    private LovGetListDto employeeDet;
    private Long allowanceDeductionPoid;
    private BigDecimal amount;
    private LocalDate payrollMonth;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}
