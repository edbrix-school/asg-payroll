package com.asg.payroll.salarydetails.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDetailResponse {

    private Long salaryPoid;
    private Long employeePoid;
    
    // Read only fields from employee/master
    private String employeeName;
    private String employeeCode;
    private LocalDate joinDate;
    private String designation;
    private String ticketDetails;
    
    private LocalDate lastIncrementDate;
    private LocalDate nextIncrementDate;
    private LocalDate indemnityDueFrom;
    
    private Long basicSalary;
    private Long registeredSalary; // Gosi Salary
    private String gosiType;
    
    private Long grossSalary;
    private Long netSalary;
    private Long totalAllowance;
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
    private Long indemnityPaidAmt;
    private String indemnityPaidDet;
    private Long loanDeductionAmt;
    private Long accommodationCost;
    private String remarks;

    private List<SalaryAllowanceDto> allowances;
    private List<SalaryHistoryDto> history;
}
