package com.asg.payroll.employeeSettlement.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="HR_LEAVE_SETTLEMENT_HDR")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSettlementDtl extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID",nullable = false)
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "DOC_REF", length = 25)
    private String docRef;

    @Column(name = "EMPLOYEE_POID")
    private Long employeePoid;

    @Column(name = "SETTLEMENT_TYPE", length = 20)
    private String settlementType;

    @Column(name = "LEAVE_TYPE", length = 20)
    private String leaveType;

    @Column(name = "LEAVE_REQUEST_POID")
    private Long leaveRequestPoid;

    @Column(name = "LEAVE_APPROVAL_FORM_ATTACHED", length = 1)
    private String leaveApprovalFormAttached;

    @Column(name = "HOME_COUNTRY_PHONE", length = 20)
    private String homeCountryPhone;

    @Column(name = "LEAVE_START_DATE")
    private LocalDate leaveStartDate;

    @Column(name = "LEAVE_END_DATE")
    private LocalDate leaveEndDate;

    @Column(name = "REJOIN_DATE")
    private LocalDate rejoinDate;

    @Column(name = "LEAVE_DAYS")
    private Long leaveDays;

    @Column(name = "ELIGIBLE_LEAVE_DAYS")
    private Long eligibleLeaveDays;

    @Column(name = "BALANCE_LEAVE_DAYS")
    private Long balanceLeaveDays;

    @Column(name = "LEAVE_SALARY")
    private BigDecimal leaveSalary;

    @Column(name = "TICKET_ELIGIBLITY", length = 200)
    private String ticketEligiblity;

    @Column(name = "TICKET_ISSUE_TYPE", length = 20)
    private String ticketIssueType;

    @Column(name = "TICKET_ISSUED_COUNT")
    private Long ticketIssuedCount;

    @Column(name = "TICKET_ENCASHMENT")
    private BigDecimal ticketEncashment;

    @Column(name = "AIR_SECTOR_POID")
    private Long airSectorPoid;

    @Column(name = "ATTENDANCE_POID")
    private Long attendancePoid;

    @Column(name = "BASIC_SALARY")
    private BigDecimal basicSalary;

    @Column(name = "BASIC_SALARY_PAYABLE")
    private BigDecimal basicSalaryPayable;

    @Column(name = "WORKED_DAYS")
    private Long workedDays;

    @Column(name = "ABSENT_DAYS")
    private Long absentDays;

    @Column(name = "ABSENT_AMOUNT")
    private BigDecimal absentAmount;

    @Column(name = "OT1_HRS")
    private Long ot1Hrs;

    @Column(name = "OT1_AMT")
    private Long ot1Amt;

    @Column(name = "OT2_HRS")
    private Long ot2Hrs;

    @Column(name = "OT2_AMT")
    private Long ot2Amt;

    @Column(name = "SHORT_HRS")
    private Long shortHrs;

    @Column(name = "SHORT_HRS_AMOUNT")
    private Long shortHrsAmount;

    @Column(name = "MEDICAL_DAYS")
    private Long medicalDays;

    @Column(name = "FIXED_ALLOWANCE")
    private Long fixedAllowance;

    @Column(name = "HRA_ALLOWANCE")
    private Long hraAllowance;

    @Column(name = "TRAVEL_ALLOWANCE")
    private Long travelAllowance;

    @Column(name = "COMMUNICATION_ALLOWANCE")
    private Long communicationAllowance;

    @Column(name = "TRANSPORT_ALLOWANCE")
    private Long transportAllowance;

    @Column(name = "SPECIAL_ALLOWANCE")
    private Long specialAllowance;

    @Column(name = "OTHER_ALLOWANCE")
    private Long otherAllowance;

    @Column(name = "OTHER_ALLOW_GL")
    private Long otherAllowGl;

    @Column(name = "ARREARS")
    private Long arrears;

    @Column(name = "GOSI_DEDUCTION")
    private Long gosiDeduction;

    @Column(name = "LOAN_DEDUCTION")
    private Long loanDeduction;

    @Column(name = "PHONE_DEDUCTION")
    private Long phoneDeduction;

    @Column(name = "MEDICAL_INSURANCE")
    private Long medicalInsurance;

    @Column(name = "RECURRING_DEDUCTION")
    private Long recurringDeduction;

    @Column(name = "OTHER_DEDUCTIONS")
    private Long otherDeductions;

    @Column(name = "OTHER_DED_GL")
    private Long otherDedGl;

    @Column(name = "TOT_ALLOWANCE")
    private Long totAllowance;

    @Column(name = "TOT_DEDUCTIONS")
    private Long totDeductions;

    @Column(name = "GROSS_SALARY")
    private BigDecimal grossSalary;

    @Column(name = "NET_SALARY")
    private BigDecimal netSalary;

    @Column(name = "DATE_OF_LEAVING")
    private LocalDate dateOfLeaving;

    @Column(name = "ASSET_HANDOVER_FORM_ATTACH", length = 1)
    private String assetHandoverFormAttach;

    @Column(name = "REASON_FOR_LEAVING", length = 500)
    private String reasonForLeaving;

    @Column(name = "TOTAL_YEARS_SERVICE")
    private Long totalYearsService;

    @Column(name = "TOTAL_INDEMNITY_DAYS")
    private Long totalIndemnityDays;

    @Column(name = "INDEMNITY_IN_THREE_YR")
    private Long indemnityInThreeYr;

    @Column(name = "INDEMNITY_AFTER_THREE_YR")
    private Long indemnityAfterThreeYr;

    @Column(name = "TOTAL_INDEMNITY_AMOUNT")
    private Long totalIndemnityAmount;

    @Column(name = "PREV_INDEMNITY_DATE")
    private LocalDate prevIndemnityDate;

    @Column(name = "TOTAL_SETTLEMENT_AMT")
    private BigDecimal totalSettlementAmt;

    @Column(name = "PAYMENT_METHOD", length = 20)
    private String paymentMethod;

    @Column(name = "SETTLEMENT_STATUS", length = 20)
    private String settlementStatus;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "LAST_WORKING_DATE")
    private LocalDate lastWorkingDate;

    @Column(name = "WITHOUT_SALARY", length = 1)
    private String withoutSalary;

    @Column(name = "GOSI_FROM_DATE")
    private LocalDate gosiFromDate;

    @Column(name = "GOSI_TO_DATE")
    private LocalDate gosiToDate;

    @Column(name = "BASIC_SALARY_PAYABLE2")
    private BigDecimal basicSalaryPayable2;

    @Column(name = "WORKED_DAYS2")
    private Long workedDays2;

    @Column(name = "FIXED_ALLOWANCE2")
    private Long fixedAllowance2;

    @Column(name = "HRA_ALLOWANCE2")
    private Long hraAllowance2;

    @Column(name = "TRANSPORT_ALLOWANCE2")
    private Long transportAllowance2;

    @Column(name = "SPECIAL_ALLOWANCE2")
    private Long specialAllowance2;

    @Column(name = "GOSI_DEDUCTION2")
    private Long gosiDeduction2;

    @Column(name = "LOAN_DEDUCTION2")
    private Long loanDeduction2;

    @Column(name = "MEDICAL_INSURANCE2")
    private Long medicalInsurance2;

    @Column(name = "TOT_ALLOWANCE2")
    private Long totAllowance2;

    @Column(name = "TOT_DEDUCTIONS2")
    private Long totDeductions2;

    @Column(name = "GROSS_SALARY2")
    private Long grossSalary2;

    @Column(name = "NET_SALARY2")
    private BigDecimal netSalary2;

    @Column(name = "TRAVEL_ALLOWANCE2")
    private Long travelAllowance2;

    @Column(name = "COMMUNICATION_ALLOWANCE2")
    private BigDecimal communicationAllowance2;

    @Column(name = "OTHER_ALLOWANCE_DETAILS", length = 100)
    private String otherAllowanceDetails;

    @Column(name = "OTHER_DEDUCTIONS_DETAILS", length = 100)
    private String otherDeductionsDetails;

    @Column(name = "PAYROLL1_DATE")
    private LocalDate payroll1Date;

    @Column(name = "PAYROLL2_DATE")
    private LocalDate payroll2Date;

    @Column(name = "JOIN_DATE")
    private LocalDate joinDate;

    @Column(name = "JV_DOC_REF", length = 20)
    private String jvDocRef;

    @Column(name = "JV_POID")
    private Long jvPoid;

    @Column(name = "LAST_LEAVE_DETAILS", length = 200)
    private String lastLeaveDetails;

    @Column(name = "WORK_DAYS_FROM_LAST_LEAVE")
    private Long workDaysFromLastLeave;

    @Column(name = "PAID_LEAVES_TAKEN")
    private Long paidLeavesTaken;

    @Column(name = "ATTENDANCE2_POID")
    private Long attendance2Poid;

    @Column(name = "FIXED_OT")
    private Long fixedOt;

    @Column(name = "FIXED_OT2")
    private Long fixedOt2;

    @Column(name = "BASIC_FIX_ALW")
    private Long basicFixAlw;

    @Column(name = "BASIC_FIX_OT")
    private Long basicFixOt;

    @Column(name = "LAST_REJOIN_DATE")
    private LocalDate lastRejoinDate;

    @Column(name = "BASIC_ELIGIBLE_LEAVE_DAYS")
    private Long basicEligibleLeaveDays;

    @Column(name = "BASIC_SAL_PER_DAY")
    private BigDecimal basicSalPerDay;

    @Column(name = "GROSS_SAL_PER_DAY")
    private BigDecimal grossSalPerDay;

    @Column(name = "BASIC_HRA")
    private Long basicHra;

    @Column(name = "BASIC_TRANSPORT")
    private Long basicTransport;

    @Column(name = "EXGRATIA")
    private Long exgratia;

    @Column(name = "EXGRATIA_GL")
    private Long exgratiaGl;

    @Column(name = "ROUND_OFF")
    private Long roundOff;

    @Column(name = "PAYMENT_DOC_TYPE", length = 20)
    private String paymentDocType;

    @Column(name = "PAYMENT_DOC_REF", length = 20)
    private String paymentDocRef;

    @Column(name = "PAYMENT_DOC_POID")
    private Long paymentDocPoid;

    @Column(name = "PAYMENT_VALUE_DATE")
    private LocalDate paymentValueDate;

    @Column(name = "PAYMENT_PAYEE_NAME", length = 100)
    private String paymentPayeeName;

    @Column(name = "PAYMENT_BANK_POID")
    private Long paymentBankPoid;

    @Column(name = "PAYMENT_PRE_PRINTED", length = 1)
    private String paymentPrePrinted;

    @Column(name = "CR_POID")
    private Long crPoid;

    @Column(name = "INDEMNITY_FIRST_THREE_YR_DAYS", length = 100)
    private String indemnityFirstThreeYrDays;

    @Column(name = "INDEMNITY_AFTER_THREE_YR_DAYS", length = 100)
    private String indemnityAfterThreeYrDays;

    @Column(name = "INDEMNITY_SERVICE_DAYS")
    private Long indemnityServiceDays;

    @Column(name = "OTHER_ALLOWANCE2")
    private Long otherAllowance2;

    @Column(name = "OTHER_ALLOW_GL2")
    private Long otherAllowGl2;

    @Column(name = "OTHER_ALLOWANCE_DETAILS2", length = 100)
    private String otherAllowanceDetails2;

    @Column(name = "LEAVE_ABSENT_DAYS")
    private Long leaveAbsentDays;

    @Column(name = "LEAVE_WORK_DAYS_STAGE1")
    private Long leaveWorkDaysStage1;

    @Column(name = "LEAVE_ELIGIBLE_LEAVE_STAGE1")
    private Long leaveEligibleLeaveStage1;

    @Column(name = "LAST_TICKET_DETAILS", length = 200)
    private String lastTicketDetails;

    @Column(name = "LATE_DEDUCTION_AMT")
    private Long lateDeductionAmt;

    @Column(name = "TICKETS_EARNED")
    private Long ticketsEarned;

    @Column(name = "EXCESS_VISA_CHARGES")
    private BigDecimal excessVisaCharges;

    @Column(name = "TICKET_TILL_DATE")
    private LocalDate ticketTillDate;

    @Column(name = "INDEMNITY_PAID_AMT")
    private BigDecimal indemnityPaidAmt;

    @Column(name = "INDEMNITY_WITHOUT_PAY_DAYS")
    private Long indemnityWithoutPayDays;

    @Column(name = "RECOV_VISA_COST", length = 1)
    private String recovVisaCost;

    @Column(name = "RECOV_GOSI_COST", length = 1)
    private String recovGosiCost;

    @Column(name = "RECOV_LMRA_COST", length = 1)
    private String recovLmraCost;

    @Column(name = "RECOV_MDINS_COST", length = 1)
    private String recovMdinsCost;

    @Column(name = "RECOV_TICKET_COST", length = 1)
    private String recovTicketCost;

    @Column(name = "TICKET_ELIGIBLITY_TEMP")
    private Long ticketEligiblityTemp;

    @Column(name = "INDEMNITY_ELIGIBLE_DAYS")
    private Long indemnityEligibleDays;

    @Column(name = "SIO_SHARE_INDEMNITY")
    private Long sioShareIndemnity;

    @Column(name = "INDMNTY_MNTHS_COMPANY", length = 100)
    private String indmntyMnthsCompany;

    @Column(name = "INDMNTY_MNTHS_SIO", length = 100)
    private String indmntyMnthsSio;

    @Column(name = "INDEMNITY_PROV_AMOUNT")
    private Long indemnityProvAmount;
}
