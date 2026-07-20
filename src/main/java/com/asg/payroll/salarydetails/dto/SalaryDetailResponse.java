package com.asg.payroll.salarydetails.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDetailResponse {

    private Long salaryPoid;
    private Long employeePoid;
    private LocalDateTime createdDate;
    private String createdBy;
    
    // Read only fields from employee/master
    private String employeeName;
    private String employeeCode;
    private LocalDate joinDate;
    private String designation;
    private String ticketDetails;
    
    private LocalDate lastIncrementDate;
    private LocalDate nextIncrementDate;
    private LocalDate indemnityDueFrom;
    
    private BigDecimal basicSalary;
    private BigDecimal registeredSalary;
    private BigDecimal gosiSalary; // alias for registeredSalary used by FE
    private String gosiType;
    
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private BigDecimal totAllowance;
    private String ctcAmount; // From FUNC_EMPLOYEE_RPT_CTC

    private String paymentMethod;
    private Long bankPoid;
    private String bankName;
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
    private List<SalaryHistoryDto> history;
}
