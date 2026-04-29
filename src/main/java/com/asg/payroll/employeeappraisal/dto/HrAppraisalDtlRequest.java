package com.asg.payroll.employeeappraisal.dto;

import com.asg.payroll.employeeappraisal.enums.ActionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrAppraisalDtlRequest {
    private Long detRowId;
    @NotNull(message = "Employee is required.")
    private Long employeePoid;
    private Long designationPoid;
    private LocalDate joinDate;
    @Size(max = 20, message = "Current air entitlement must not exceed 20 characters.")
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
    @Size(max = 20, message = "New air entitlement must not exceed 20 characters.")
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
    @Size(max = 20, message = "Status must not exceed 20 characters.")
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
    @Size(max = 50, message = "Grid listing method must not exceed 50 characters.")
    private String gridListingMethod;
    private BigDecimal registeredSalary;
    private BigDecimal curMonthlyCtc;
    private BigDecimal curYearlyCtc;
    private BigDecimal newMonthlyCtc;
    private BigDecimal newYearlyCtc;
    private Long lastDesignationPoid;
    private LocalDate lastPromotionDate;
    private ActionType actionType;
}
