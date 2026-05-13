package com.asg.payroll.salarydetails.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDetailRequest {

    private Long employeePoid;
    private LocalDate lastIncrementDate;
    private LocalDate nextIncrementDate;
    private LocalDate indemnityDueFrom;
    
    private BigDecimal basicSalary;
    private BigDecimal registeredSalary; // Gosi Salary
    private String gosiType;
    
    private String paymentMethod;
    private Long bankPoid;
    private String ibanAccountNo;
    private String bankRegistrationId;
    private String accountName;

    private String bankGuarantee;
    private String bankGuaranteeDetails;
    private LocalDate bankGuaranteeDueDate;

    private String contractPrintType;
    private BigDecimal indemnityPaidAmt;
    private String indemnityPaidDet;
    private BigDecimal loanDeductionAmt;
    private BigDecimal accommodationCost;
    private String remarks;

    private List<SalaryAllowanceDto> allowances;
}
