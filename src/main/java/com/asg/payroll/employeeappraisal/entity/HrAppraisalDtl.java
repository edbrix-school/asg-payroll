package com.asg.payroll.employeeappraisal.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "HR_APPRAISAL_DTL")
@Getter
@Setter
@IdClass(HrAppraisalDtlId.class)
public class HrAppraisalDtl extends BaseEntity {

    @Id
    @AuditIgnore
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @Id
    @AuditIgnore
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "EMPLOYEE_POID")
    private Long employeePoid;

    @Column(name = "DESIGNATION_POID")
    private Long designationPoid;

    @Column(name = "JOIN_DATE")
    private LocalDate joinDate;

    @Column(name = "CUR_AIR_ENTITLE", length = 20)
    private String curAirEntitle;

    @Column(name = "CUR_BONUS")
    private BigDecimal curBonus;

    @Column(name = "CUR_INCREMENT")
    private BigDecimal curIncrement;

    @Column(name = "CUR_BASIC_SALARY")
    private BigDecimal curBasicSalary;

    @Column(name = "CUR_FA_ALW")
    private BigDecimal curFaAlw;

    @Column(name = "CUR_TA_ALW")
    private BigDecimal curTaAlw;

    @Column(name = "CUR_HRA_ALW")
    private BigDecimal curHraAlw;

    @Column(name = "CUR_FIXOT_ALW")
    private BigDecimal curFixotAlw;

    @Column(name = "CUR_SPL_ALW")
    private BigDecimal curSplAlw;

    @Column(name = "CUR_OTH_ALW")
    private BigDecimal curOthAlw;

    @Column(name = "CUR_AVGOT")
    private BigDecimal curAvgot;

    @Column(name = "CUR_GROSS_PAY")
    private BigDecimal curGrossPay;

    @Column(name = "NEW_AIR_ENTITLE", length = 20)
    private String newAirEntitle;

    @Column(name = "NEW_BONUS")
    private BigDecimal newBonus;

    @Column(name = "NEW_INCREMENT_PER")
    private BigDecimal newIncrementPer;

    @Column(name = "NEW_BASIC_SALARY")
    private BigDecimal newBasicSalary;

    @Column(name = "NEW_FA_ALW")
    private BigDecimal newFaAlw;

    @Column(name = "NEW_TA_ALW")
    private BigDecimal newTaAlw;

    @Column(name = "NEW_HRA_ALW")
    private BigDecimal newHraAlw;

    @Column(name = "NEW_FIXOT_ALW")
    private BigDecimal newFixotAlw;

    @Column(name = "NEW_SPL_ALW")
    private BigDecimal newSplAlw;

    @Column(name = "NEW_OTH_ALW")
    private BigDecimal newOthAlw;

    @Column(name = "NEW_AVGOT")
    private BigDecimal newAvgot;

    @Column(name = "NEW_GROSS_PAY")
    private BigDecimal newGrossPay;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "NET_INCREMENT")
    private BigDecimal netIncrement;

    @Column(name = "LAST_INCREMENT_DATE")
    private LocalDate lastIncrementDate;

    @Column(name = "LAST_INCREMENT_AMT")
    private BigDecimal lastIncrementAmt;

    @Column(name = "LAST_BONUS")
    private BigDecimal lastBonus;

    @Column(name = "CUR_TICKET_PERIOD")
    private BigDecimal curTicketPeriod;

    @Column(name = "CUR_NO_OF_TICKETS")
    private BigDecimal curNoOfTickets;

    @Column(name = "NEW_TICKET_PERIOD")
    private BigDecimal newTicketPeriod;

    @Column(name = "NEW_NO_OF_TICKETS")
    private BigDecimal newNoOfTickets;

    @Column(name = "NEW_DESIGNATION_POID")
    private Long newDesignationPoid;

    @Column(name = "ARREARS")
    private BigDecimal arrears;

    @Column(name = "NEW_BONUS_PER")
    private BigDecimal newBonusPer;

    @Column(name = "LETTER_EMAILED_ON")
    private LocalDate letterEmailedOn;

    @Column(name = "GRID_LISTING_METHOD", length = 50)
    private String gridListingMethod;

    @Column(name = "REGISTERED_SALARY")
    private BigDecimal registeredSalary;

    @Column(name = "CUR_MONTHLY_CTC")
    private BigDecimal curMonthlyCtc;

    @Column(name = "CUR_YEARLY_CTC")
    private BigDecimal curYearlyCtc;

    @Column(name = "NEW_MONTHLY_CTC")
    private BigDecimal newMonthlyCtc;

    @Column(name = "NEW_YEARLY_CTC")
    private BigDecimal newYearlyCtc;

    @Column(name = "LAST_DESIGNATION_POID")
    private Long lastDesignationPoid;

    @Column(name = "LAST_PROMOTION_DATE")
    private LocalDate lastPromotionDate;
}