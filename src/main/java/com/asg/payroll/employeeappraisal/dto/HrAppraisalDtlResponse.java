package com.asg.payroll.employeeappraisal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class HrAppraisalDtlResponse {

    private Long transactionPoid;
    private Long detRowId;
    private Long employeePoid;
    private String employeeCode;
    private String employeeName;
    private String employeeName2;
    private Long designationPoid;
    private LocalDate joinDate;
    private String curAirEntitle;
    private BigDecimal curBonus;
    private BigDecimal curIncrement;
    private BigDecimal curBasicSalary;
    private BigDecimal curFaAlw;
    private BigDecimal curTaAlw;
    private BigDecimal curHraAlw;
    private BigDecimal curFixotAlw;
    private BigDecimal curSplAlw;
    private BigDecimal curOthAlw;
    private BigDecimal curAvgot;
    private BigDecimal curGrossPay;
    private String newAirEntitle;
    private BigDecimal newBonus;
    private BigDecimal newIncrementPer;
    private BigDecimal newBasicSalary;
    private BigDecimal newFaAlw;
    private BigDecimal newTaAlw;
    private BigDecimal newHraAlw;
    private BigDecimal newFixotAlw;
    private BigDecimal newSplAlw;
    private BigDecimal newOthAlw;
    private BigDecimal newAvgot;
    private BigDecimal newGrossPay;
    private String status;
    private BigDecimal netIncrement;
    private LocalDate lastIncrementDate;
    private BigDecimal lastIncrementAmt;
    private BigDecimal lastBonus;
    private BigDecimal curTicketPeriod;
    private BigDecimal curNoOfTickets;
    private BigDecimal newTicketPeriod;
    private BigDecimal newNoOfTickets;
    private Long newDesignationPoid;
    private BigDecimal arrears;
    private BigDecimal newBonusPer;
    private LocalDate letterEmailedOn;
    private String gridListingMethod;
    private BigDecimal registeredSalary;
    private BigDecimal curMonthlyCtc;
    private BigDecimal curYearlyCtc;
    private BigDecimal newMonthlyCtc;
    private BigDecimal newYearlyCtc;
    private Long lastDesignationPoid;
    private LocalDate lastPromotionDate;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastmodifiedBy;
    private LocalDateTime lastmodifiedDate;
}
