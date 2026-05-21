package com.asg.payroll.employeeSettlement.util;
import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.service.LovDataService;
import com.asg.payroll.employeeSettlement.dto.EmployeeSettlementDto;
import com.asg.payroll.employeeSettlement.dto.LoanDeductionDto;
import com.asg.payroll.employeeSettlement.entity.EmployeeSettlementDtl;
import com.asg.payroll.employeeSettlement.entity.LoanDeductionDtl;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeSettlementMapper {

    public static EmployeeSettlementDto mapToDto(EmployeeSettlementDtl entity,LovDataService lovDataService) {
        if (entity == null) {
            return null;
        }

        return EmployeeSettlementDto.builder()
                .transactionPoid(entity.getTransactionPoid())
                .transactionDate(entity.getTransactionDate())
                .groupPoid(entity.getGroupPoid())
                .companyPoid(entity.getCompanyPoid())
                .docRef(entity.getDocRef())
                .employeePoid(entity.getEmployeePoid())
                .settlementType(entity.getSettlementType())
                .leaveType(entity.getLeaveType())
                .leaveRequestPoid(entity.getLeaveRequestPoid())
                .leaveApprovalFormAttached(entity.getLeaveApprovalFormAttached())
                .homeCountryPhone(entity.getHomeCountryPhone())
                .leaveStartDate(entity.getLeaveStartDate())
                .leaveEndDate(entity.getLeaveEndDate())
                .rejoinDate(entity.getRejoinDate())
                .leaveDays(entity.getLeaveDays())
                .eligibleLeaveDays(entity.getEligibleLeaveDays())
                .balanceLeaveDays(entity.getBalanceLeaveDays())
                .leaveSalary(entity.getLeaveSalary())
                .ticketEligiblity(entity.getTicketEligiblity())
                .ticketIssueType(entity.getTicketIssueType())
                .ticketIssuedCount(entity.getTicketIssuedCount())
                .ticketEncashment(entity.getTicketEncashment())
                .airSectorPoid(entity.getAirSectorPoid())
                .attendancePoid(entity.getAttendancePoid())
                .basicSalary(entity.getBasicSalary())
                .basicSalaryPayable(entity.getBasicSalaryPayable())
                .workedDays(entity.getWorkedDays())
                .absentDays(entity.getAbsentDays())
                .absentAmount(entity.getAbsentAmount())
                .ot1Hrs(entity.getOt1Hrs())
                .ot1Amt(entity.getOt1Amt())
                .ot2Hrs(entity.getOt2Hrs())
                .ot2Amt(entity.getOt2Amt())
                .shortHrs(entity.getShortHrs())
                .shortHrsAmount(entity.getShortHrsAmount())
                .medicalDays(entity.getMedicalDays())
                .fixedAllowance(entity.getFixedAllowance())
                .hraAllowance(entity.getHraAllowance())
                .travelAllowance(entity.getTravelAllowance())
                .communicationAllowance(entity.getCommunicationAllowance())
                .transportAllowance(entity.getTransportAllowance())
                .specialAllowance(entity.getSpecialAllowance())
                .otherAllowance(entity.getOtherAllowance())
                .otherAllowGl(entity.getOtherAllowGl())
                .arrears(entity.getArrears())
                .gosiDeduction(entity.getGosiDeduction())
                .loanDeduction(entity.getLoanDeduction())
                .phoneDeduction(entity.getPhoneDeduction())
                .medicalInsurance(entity.getMedicalInsurance())
                .recurringDeduction(entity.getRecurringDeduction())
                .otherDeductions(entity.getOtherDeductions())
                .otherDedGl(entity.getOtherDedGl())
                .totAllowance(entity.getTotAllowance())
                .totDeductions(entity.getTotDeductions())
                .grossSalary(entity.getGrossSalary())
                .netSalary(entity.getNetSalary())
                .dateOfLeaving(entity.getDateOfLeaving())
                .assetHandoverFormAttach(entity.getAssetHandoverFormAttach())
                .reasonForLeaving(entity.getReasonForLeaving())
                .totalYearsService(entity.getTotalYearsService())
                .totalIndemnityDays(entity.getTotalIndemnityDays())
                .indemnityInThreeYr(entity.getIndemnityInThreeYr())
                .indemnityAfterThreeYr(entity.getIndemnityAfterThreeYr())
                .totalIndemnityAmount(entity.getTotalIndemnityAmount())
                .prevIndemnityDate(entity.getPrevIndemnityDate())
                .totalSettlementAmt(entity.getTotalSettlementAmt())
                .paymentMethod(entity.getPaymentMethod())
                .settlementStatus(entity.getSettlementStatus())
                .remarks(entity.getRemarks())
                .deleted(entity.getDeleted())
                .lastWorkingDate(entity.getLastWorkingDate())
                .withoutSalary(entity.getWithoutSalary())
                .gosiFromDate(entity.getGosiFromDate())
                .gosiToDate(entity.getGosiToDate())
                .basicSalaryPayable2(entity.getBasicSalaryPayable2())
                .workedDays2(entity.getWorkedDays2())
                .fixedAllowance2(entity.getFixedAllowance2())
                .hraAllowance2(entity.getHraAllowance2())
                .transportAllowance2(entity.getTransportAllowance2())
                .specialAllowance2(entity.getSpecialAllowance2())
                .gosiDeduction2(entity.getGosiDeduction2())
                .loanDeduction2(entity.getLoanDeduction2())
                .medicalInsurance2(entity.getMedicalInsurance2())
                .totAllowance2(entity.getTotAllowance2())
                .totDeductions2(entity.getTotDeductions2())
                .grossSalary2(entity.getGrossSalary2())
                .netSalary2(entity.getNetSalary2())
                .travelAllowance2(entity.getTravelAllowance2())
                .communicationAllowance2(entity.getCommunicationAllowance2())
                .otherAllowanceDetails(entity.getOtherAllowanceDetails())
                .otherDeductionsDetails(entity.getOtherDeductionsDetails())
                .payroll1Date(entity.getPayroll1Date())
                .payroll2Date(entity.getPayroll2Date())
                .joinDate(entity.getJoinDate())
                .jvDocRef(entity.getJvDocRef())
                .jvPoid(entity.getJvPoid())
                .lastLeaveDetails(entity.getLastLeaveDetails())
                .workDaysFromLastLeave(entity.getWorkDaysFromLastLeave())
                .paidLeavesTaken(entity.getPaidLeavesTaken())
                .attendance2Poid(entity.getAttendance2Poid())
                .fixedOt(entity.getFixedOt())
                .fixedOt2(entity.getFixedOt2())
                .basicFixAlw(entity.getBasicFixAlw())
                .basicFixOt(entity.getBasicFixOt())
                .lastRejoinDate(entity.getLastRejoinDate())
                .basicEligibleLeaveDays(entity.getBasicEligibleLeaveDays())
                .basicSalPerDay(entity.getBasicSalPerDay())
                .grossSalPerDay(entity.getGrossSalPerDay())
                .basicHra(entity.getBasicHra())
                .basicTransport(entity.getBasicTransport())
                .exgratia(entity.getExgratia())
                .exgratiaGl(entity.getExgratiaGl())
                .roundOff(entity.getRoundOff())
                .paymentDocType(entity.getPaymentDocType())
                .paymentDocRef(entity.getPaymentDocRef())
                .paymentDocPoid(entity.getPaymentDocPoid())
                .paymentValueDate(entity.getPaymentValueDate())
                .paymentPayeeName(entity.getPaymentPayeeName())
                .paymentBankPoid(entity.getPaymentBankPoid())
                .paymentPrePrinted(entity.getPaymentPrePrinted())
                .crPoid(entity.getCrPoid())
                .indemnityFirstThreeYrDays(entity.getIndemnityFirstThreeYrDays())
                .indemnityAfterThreeYrDays(entity.getIndemnityAfterThreeYrDays())
                .indemnityServiceDays(entity.getIndemnityServiceDays())
                .otherAllowance2(entity.getOtherAllowance2())
                .otherAllowGl2(entity.getOtherAllowGl2())
                .otherAllowanceDetails2(entity.getOtherAllowanceDetails2())
                .leaveAbsentDays(entity.getLeaveAbsentDays())
                .leaveWorkDaysStage1(entity.getLeaveWorkDaysStage1())
                .leaveEligibleLeaveStage1(entity.getLeaveEligibleLeaveStage1())
                .lastTicketDetails(entity.getLastTicketDetails())
                .lateDeductionAmt(entity.getLateDeductionAmt())
                .ticketsEarned(entity.getTicketsEarned())
                .excessVisaCharges(entity.getExcessVisaCharges())
                .ticketTillDate(entity.getTicketTillDate())
                .indemnityPaidAmt(entity.getIndemnityPaidAmt())
                .indemnityWithoutPayDays(entity.getIndemnityWithoutPayDays())
                .recovVisaCost(entity.getRecovVisaCost())
                .recovGosiCost(entity.getRecovGosiCost())
                .recovLmraCost(entity.getRecovLmraCost())
                .recovMdinsCost(entity.getRecovMdinsCost())
                .recovTicketCost(entity.getRecovTicketCost())
                .ticketEligiblityTemp(entity.getTicketEligiblityTemp())
                .indemnityEligibleDays(entity.getIndemnityEligibleDays())
                .sioShareIndemnity(entity.getSioShareIndemnity())
                .indmntyMnthsCompany(entity.getIndmntyMnthsCompany())
                .indmntyMnthsSio(entity.getIndmntyMnthsSio())
                .indemnityProvAmount(entity.getIndemnityProvAmount())
                .build();
    }

    public static void mapCreateDtoToEntity(EmployeeSettlementDto dto, EmployeeSettlementDtl entity) {

        entity.setGroupPoid(dto.getGroupPoid());
        entity.setCompanyPoid(dto.getCompanyPoid());
        entity.setTransactionDate(dto.getTransactionDate());
        entity.setDocRef(dto.getDocRef());
        entity.setEmployeePoid(dto.getEmployeePoid());
        entity.setSettlementType(dto.getSettlementType());
        entity.setLeaveType(dto.getLeaveType());
        entity.setLeaveRequestPoid(dto.getLeaveRequestPoid());
        entity.setLeaveApprovalFormAttached(dto.getLeaveApprovalFormAttached());
        entity.setHomeCountryPhone(dto.getHomeCountryPhone());
        entity.setLeaveStartDate(dto.getLeaveStartDate());
        entity.setLeaveEndDate(dto.getLeaveEndDate());
        entity.setRejoinDate(dto.getRejoinDate());
        entity.setLeaveDays(dto.getLeaveDays());
        entity.setEligibleLeaveDays(dto.getEligibleLeaveDays());
        entity.setBalanceLeaveDays(dto.getBalanceLeaveDays());
        entity.setLeaveSalary(dto.getLeaveSalary());
        entity.setTicketEligiblity(dto.getTicketEligiblity());
        entity.setTicketIssueType(dto.getTicketIssueType());
        entity.setTicketIssuedCount(dto.getTicketIssuedCount());
        entity.setTicketEncashment(dto.getTicketEncashment());
        entity.setAirSectorPoid(dto.getAirSectorPoid());
        entity.setAttendancePoid(dto.getAttendancePoid());
        entity.setBasicSalary(dto.getBasicSalary());
        entity.setBasicSalaryPayable(dto.getBasicSalaryPayable());
        entity.setWorkedDays(dto.getWorkedDays());
        entity.setAbsentDays(dto.getAbsentDays());
        entity.setAbsentAmount(dto.getAbsentAmount());
        entity.setOt1Hrs(dto.getOt1Hrs());
        entity.setOt1Amt(dto.getOt1Amt());
        entity.setOt2Hrs(dto.getOt2Hrs());
        entity.setOt2Amt(dto.getOt2Amt());
        entity.setShortHrs(dto.getShortHrs());
        entity.setShortHrsAmount(dto.getShortHrsAmount());
        entity.setMedicalDays(dto.getMedicalDays());
        entity.setFixedAllowance(dto.getFixedAllowance());
        entity.setHraAllowance(dto.getHraAllowance());
        entity.setTravelAllowance(dto.getTravelAllowance());
        entity.setCommunicationAllowance(dto.getCommunicationAllowance());
        entity.setTransportAllowance(dto.getTransportAllowance());
        entity.setSpecialAllowance(dto.getSpecialAllowance());
        entity.setOtherAllowance(dto.getOtherAllowance());
        entity.setOtherAllowGl(dto.getOtherAllowGl());
        entity.setArrears(dto.getArrears());
        entity.setGosiDeduction(dto.getGosiDeduction());
        entity.setLoanDeduction(dto.getLoanDeduction());
        entity.setPhoneDeduction(dto.getPhoneDeduction());
        entity.setMedicalInsurance(dto.getMedicalInsurance());
        entity.setRecurringDeduction(dto.getRecurringDeduction());
        entity.setOtherDeductions(dto.getOtherDeductions());
        entity.setOtherDedGl(dto.getOtherDedGl());
        entity.setTotAllowance(dto.getTotAllowance());
        entity.setTotDeductions(dto.getTotDeductions());
        entity.setGrossSalary(dto.getGrossSalary());
        entity.setNetSalary(dto.getNetSalary());
        entity.setDateOfLeaving(dto.getDateOfLeaving());
        entity.setAssetHandoverFormAttach(dto.getAssetHandoverFormAttach());
        entity.setReasonForLeaving(dto.getReasonForLeaving());
        entity.setTotalYearsService(dto.getTotalYearsService());
        entity.setTotalIndemnityDays(dto.getTotalIndemnityDays());
        entity.setIndemnityInThreeYr(dto.getIndemnityInThreeYr());
        entity.setIndemnityAfterThreeYr(dto.getIndemnityAfterThreeYr());
        entity.setTotalIndemnityAmount(dto.getTotalIndemnityAmount());
        entity.setPrevIndemnityDate(dto.getPrevIndemnityDate());
        entity.setTotalSettlementAmt(dto.getTotalSettlementAmt());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setSettlementStatus(dto.getSettlementStatus());
        entity.setRemarks(dto.getRemarks());
        entity.setLastWorkingDate(dto.getLastWorkingDate());
        entity.setWithoutSalary(dto.getWithoutSalary());
        entity.setGosiFromDate(dto.getGosiFromDate());
        entity.setGosiToDate(dto.getGosiToDate());
        entity.setBasicSalaryPayable2(dto.getBasicSalaryPayable2());
        entity.setWorkedDays2(dto.getWorkedDays2());
        entity.setFixedAllowance2(dto.getFixedAllowance2());
        entity.setHraAllowance2(dto.getHraAllowance2());
        entity.setTransportAllowance2(dto.getTransportAllowance2());
        entity.setSpecialAllowance2(dto.getSpecialAllowance2());
        entity.setGosiDeduction2(dto.getGosiDeduction2());
        entity.setLoanDeduction2(dto.getLoanDeduction2());
        entity.setMedicalInsurance2(dto.getMedicalInsurance2());
        entity.setTotAllowance2(dto.getTotAllowance2());
        entity.setTotDeductions2(dto.getTotDeductions2());
        entity.setGrossSalary2(dto.getGrossSalary2());
        entity.setNetSalary2(dto.getNetSalary2());
        entity.setTravelAllowance2(dto.getTravelAllowance2());
        entity.setCommunicationAllowance2(dto.getCommunicationAllowance2());
        entity.setOtherAllowanceDetails(dto.getOtherAllowanceDetails());
        entity.setOtherDeductionsDetails(dto.getOtherDeductionsDetails());
        entity.setPayroll1Date(dto.getPayroll1Date());
        entity.setPayroll2Date(dto.getPayroll2Date());
        entity.setJoinDate(dto.getJoinDate());
        entity.setJvDocRef(dto.getJvDocRef());
        entity.setJvPoid(dto.getJvPoid());
        entity.setLastLeaveDetails(dto.getLastLeaveDetails());
        entity.setWorkDaysFromLastLeave(dto.getWorkDaysFromLastLeave());
        entity.setPaidLeavesTaken(dto.getPaidLeavesTaken());
        entity.setAttendance2Poid(dto.getAttendance2Poid());
        entity.setFixedOt(dto.getFixedOt());
        entity.setFixedOt2(dto.getFixedOt2());
        entity.setBasicFixAlw(dto.getBasicFixAlw());
        entity.setBasicFixOt(dto.getBasicFixOt());
        entity.setLastRejoinDate(dto.getLastRejoinDate());
        entity.setBasicEligibleLeaveDays(dto.getBasicEligibleLeaveDays());
        entity.setBasicSalPerDay(dto.getBasicSalPerDay());
        entity.setGrossSalPerDay(dto.getGrossSalPerDay());
        entity.setBasicHra(dto.getBasicHra());
        entity.setBasicTransport(dto.getBasicTransport());
        entity.setExgratia(dto.getExgratia());
        entity.setExgratiaGl(dto.getExgratiaGl());
        entity.setRoundOff(dto.getRoundOff());
        entity.setPaymentDocType(dto.getPaymentDocType());
        entity.setPaymentDocRef(dto.getPaymentDocRef());
        entity.setPaymentDocPoid(dto.getPaymentDocPoid());
        entity.setPaymentValueDate(dto.getPaymentValueDate());
        entity.setPaymentPayeeName(dto.getPaymentPayeeName());
        entity.setPaymentBankPoid(dto.getPaymentBankPoid());
        entity.setPaymentPrePrinted(dto.getPaymentPrePrinted());
        entity.setCrPoid(dto.getCrPoid());
        entity.setIndemnityFirstThreeYrDays(dto.getIndemnityFirstThreeYrDays());
        entity.setIndemnityAfterThreeYrDays(dto.getIndemnityAfterThreeYrDays());
        entity.setIndemnityServiceDays(dto.getIndemnityServiceDays());
        entity.setOtherAllowance2(dto.getOtherAllowance2());
        entity.setOtherAllowGl2(dto.getOtherAllowGl2());
        entity.setOtherAllowanceDetails2(dto.getOtherAllowanceDetails2());
        entity.setLeaveAbsentDays(dto.getLeaveAbsentDays());
        entity.setLeaveWorkDaysStage1(dto.getLeaveWorkDaysStage1());
        entity.setLeaveEligibleLeaveStage1(dto.getLeaveEligibleLeaveStage1());
        entity.setLastTicketDetails(dto.getLastTicketDetails());
        entity.setLateDeductionAmt(dto.getLateDeductionAmt());
        entity.setTicketsEarned(dto.getTicketsEarned());
        entity.setExcessVisaCharges(dto.getExcessVisaCharges());
        entity.setTicketTillDate(dto.getTicketTillDate());
        entity.setIndemnityPaidAmt(dto.getIndemnityPaidAmt());
        entity.setIndemnityWithoutPayDays(dto.getIndemnityWithoutPayDays());
        entity.setRecovVisaCost(dto.getRecovVisaCost());
        entity.setRecovGosiCost(dto.getRecovGosiCost());
        entity.setRecovLmraCost(dto.getRecovLmraCost());
        entity.setRecovMdinsCost(dto.getRecovMdinsCost());
        entity.setRecovTicketCost(dto.getRecovTicketCost());
        entity.setTicketEligiblityTemp(dto.getTicketEligiblityTemp());
        entity.setIndemnityEligibleDays(dto.getIndemnityEligibleDays());
        entity.setSioShareIndemnity(dto.getSioShareIndemnity());
        entity.setIndmntyMnthsCompany(dto.getIndmntyMnthsCompany());
        entity.setIndmntyMnthsSio(dto.getIndmntyMnthsSio());
        entity.setIndemnityProvAmount(dto.getIndemnityProvAmount());
        entity.setDeleted("N");
    }


    public static void mapUpdateDtoToEntity(EmployeeSettlementDto dto, EmployeeSettlementDtl entity) {
        if (dto.getTransactionDate() != null) entity.setTransactionDate(dto.getTransactionDate());
        if (dto.getDocRef() != null) entity.setDocRef(dto.getDocRef());
        if (dto.getEmployeePoid() != null) entity.setEmployeePoid(dto.getEmployeePoid());
        if (dto.getSettlementType() != null) entity.setSettlementType(dto.getSettlementType());
        if (dto.getLeaveType() != null) entity.setLeaveType(dto.getLeaveType());
        if (dto.getLeaveRequestPoid() != null) entity.setLeaveRequestPoid(dto.getLeaveRequestPoid());
        if (dto.getLeaveApprovalFormAttached() != null) entity.setLeaveApprovalFormAttached(dto.getLeaveApprovalFormAttached());
        if (dto.getHomeCountryPhone() != null) entity.setHomeCountryPhone(dto.getHomeCountryPhone());
        if (dto.getLeaveStartDate() != null) entity.setLeaveStartDate(dto.getLeaveStartDate());
        if (dto.getLeaveEndDate() != null) entity.setLeaveEndDate(dto.getLeaveEndDate());
        if (dto.getRejoinDate() != null) entity.setRejoinDate(dto.getRejoinDate());
        if (dto.getLeaveDays() != null) entity.setLeaveDays(dto.getLeaveDays());
        if (dto.getEligibleLeaveDays() != null) entity.setEligibleLeaveDays(dto.getEligibleLeaveDays());
        if (dto.getBalanceLeaveDays() != null) entity.setBalanceLeaveDays(dto.getBalanceLeaveDays());
        if (dto.getLeaveSalary() != null) entity.setLeaveSalary(dto.getLeaveSalary());
        if (dto.getTicketEligiblity() != null) entity.setTicketEligiblity(dto.getTicketEligiblity());
        if (dto.getTicketIssueType() != null) entity.setTicketIssueType(dto.getTicketIssueType());
        if (dto.getTicketIssuedCount() != null) entity.setTicketIssuedCount(dto.getTicketIssuedCount());
        if (dto.getTicketEncashment() != null) entity.setTicketEncashment(dto.getTicketEncashment());
        if (dto.getAirSectorPoid() != null) entity.setAirSectorPoid(dto.getAirSectorPoid());
        if (dto.getAttendancePoid() != null) entity.setAttendancePoid(dto.getAttendancePoid());
        if (dto.getBasicSalary() != null) entity.setBasicSalary(dto.getBasicSalary());
        if (dto.getBasicSalaryPayable() != null) entity.setBasicSalaryPayable(dto.getBasicSalaryPayable());
        if (dto.getWorkedDays() != null) entity.setWorkedDays(dto.getWorkedDays());
        if (dto.getAbsentDays() != null) entity.setAbsentDays(dto.getAbsentDays());
        if (dto.getAbsentAmount() != null) entity.setAbsentAmount(dto.getAbsentAmount());
        if (dto.getOt1Hrs() != null) entity.setOt1Hrs(dto.getOt1Hrs());
        if (dto.getOt1Amt() != null) entity.setOt1Amt(dto.getOt1Amt());
        if (dto.getOt2Hrs() != null) entity.setOt2Hrs(dto.getOt2Hrs());
        if (dto.getOt2Amt() != null) entity.setOt2Amt(dto.getOt2Amt());
        if (dto.getShortHrs() != null) entity.setShortHrs(dto.getShortHrs());
        if (dto.getShortHrsAmount() != null) entity.setShortHrsAmount(dto.getShortHrsAmount());
        if (dto.getMedicalDays() != null) entity.setMedicalDays(dto.getMedicalDays());
        if (dto.getFixedAllowance() != null) entity.setFixedAllowance(dto.getFixedAllowance());
        if (dto.getHraAllowance() != null) entity.setHraAllowance(dto.getHraAllowance());
        if (dto.getTravelAllowance() != null) entity.setTravelAllowance(dto.getTravelAllowance());
        if (dto.getCommunicationAllowance() != null) entity.setCommunicationAllowance(dto.getCommunicationAllowance());
        if (dto.getTransportAllowance() != null) entity.setTransportAllowance(dto.getTransportAllowance());
        if (dto.getSpecialAllowance() != null) entity.setSpecialAllowance(dto.getSpecialAllowance());
        if (dto.getOtherAllowance() != null) entity.setOtherAllowance(dto.getOtherAllowance());
        if (dto.getOtherAllowGl() != null) entity.setOtherAllowGl(dto.getOtherAllowGl());
        if (dto.getArrears() != null) entity.setArrears(dto.getArrears());
        if (dto.getGosiDeduction() != null) entity.setGosiDeduction(dto.getGosiDeduction());
        if (dto.getLoanDeduction() != null) entity.setLoanDeduction(dto.getLoanDeduction());
        if (dto.getPhoneDeduction() != null) entity.setPhoneDeduction(dto.getPhoneDeduction());
        if (dto.getMedicalInsurance() != null) entity.setMedicalInsurance(dto.getMedicalInsurance());
        if (dto.getRecurringDeduction() != null) entity.setRecurringDeduction(dto.getRecurringDeduction());
        if (dto.getOtherDeductions() != null) entity.setOtherDeductions(dto.getOtherDeductions());
        if (dto.getOtherDedGl() != null) entity.setOtherDedGl(dto.getOtherDedGl());
        if (dto.getTotAllowance() != null) entity.setTotAllowance(dto.getTotAllowance());
        if (dto.getTotDeductions() != null) entity.setTotDeductions(dto.getTotDeductions());
        if (dto.getGrossSalary() != null) entity.setGrossSalary(dto.getGrossSalary());
        if (dto.getNetSalary() != null) entity.setNetSalary(dto.getNetSalary());
        if (dto.getDateOfLeaving() != null) entity.setDateOfLeaving(dto.getDateOfLeaving());
        if (dto.getAssetHandoverFormAttach() != null) entity.setAssetHandoverFormAttach(dto.getAssetHandoverFormAttach());
        if (dto.getReasonForLeaving() != null) entity.setReasonForLeaving(dto.getReasonForLeaving());
        if (dto.getTotalYearsService() != null) entity.setTotalYearsService(dto.getTotalYearsService());
        if (dto.getTotalIndemnityDays() != null) entity.setTotalIndemnityDays(dto.getTotalIndemnityDays());
        if (dto.getIndemnityInThreeYr() != null) entity.setIndemnityInThreeYr(dto.getIndemnityInThreeYr());
        if (dto.getIndemnityAfterThreeYr() != null) entity.setIndemnityAfterThreeYr(dto.getIndemnityAfterThreeYr());
        if (dto.getTotalIndemnityAmount() != null) entity.setTotalIndemnityAmount(dto.getTotalIndemnityAmount());
        if (dto.getPrevIndemnityDate() != null) entity.setPrevIndemnityDate(dto.getPrevIndemnityDate());
        if (dto.getTotalSettlementAmt() != null) entity.setTotalSettlementAmt(dto.getTotalSettlementAmt());
        if (dto.getPaymentMethod() != null) entity.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getSettlementStatus() != null) entity.setSettlementStatus(dto.getSettlementStatus());
        if (dto.getRemarks() != null) entity.setRemarks(dto.getRemarks());
        if (dto.getDeleted() != null) entity.setDeleted(dto.getDeleted());
        if (dto.getLastWorkingDate() != null) entity.setLastWorkingDate(dto.getLastWorkingDate());
        if (dto.getWithoutSalary() != null) entity.setWithoutSalary(dto.getWithoutSalary());
        if (dto.getGosiFromDate() != null) entity.setGosiFromDate(dto.getGosiFromDate());
        if (dto.getGosiToDate() != null) entity.setGosiToDate(dto.getGosiToDate());
        if (dto.getBasicSalaryPayable2() != null) entity.setBasicSalaryPayable2(dto.getBasicSalaryPayable2());
        if (dto.getWorkedDays2() != null) entity.setWorkedDays2(dto.getWorkedDays2());
        if (dto.getFixedAllowance2() != null) entity.setFixedAllowance2(dto.getFixedAllowance2());
        if (dto.getHraAllowance2() != null) entity.setHraAllowance2(dto.getHraAllowance2());
        if (dto.getTransportAllowance2() != null) entity.setTransportAllowance2(dto.getTransportAllowance2());
        if (dto.getSpecialAllowance2() != null) entity.setSpecialAllowance2(dto.getSpecialAllowance2());
        if (dto.getGosiDeduction2() != null) entity.setGosiDeduction2(dto.getGosiDeduction2());
        if (dto.getLoanDeduction2() != null) entity.setLoanDeduction2(dto.getLoanDeduction2());
        if (dto.getMedicalInsurance2() != null) entity.setMedicalInsurance2(dto.getMedicalInsurance2());
        if (dto.getTotAllowance2() != null) entity.setTotAllowance2(dto.getTotAllowance2());
        if (dto.getTotDeductions2() != null) entity.setTotDeductions2(dto.getTotDeductions2());
        if (dto.getGrossSalary2() != null) entity.setGrossSalary2(dto.getGrossSalary2());
        if (dto.getNetSalary2() != null) entity.setNetSalary2(dto.getNetSalary2());
        if (dto.getTravelAllowance2() != null) entity.setTravelAllowance2(dto.getTravelAllowance2());
        if (dto.getCommunicationAllowance2() != null) entity.setCommunicationAllowance2(dto.getCommunicationAllowance2());
        if (dto.getOtherAllowanceDetails() != null) entity.setOtherAllowanceDetails(dto.getOtherAllowanceDetails());
        if (dto.getOtherDeductionsDetails() != null) entity.setOtherDeductionsDetails(dto.getOtherDeductionsDetails());
        if (dto.getPayroll1Date() != null) entity.setPayroll1Date(dto.getPayroll1Date());
        if (dto.getPayroll2Date() != null) entity.setPayroll2Date(dto.getPayroll2Date());
        if (dto.getJoinDate() != null) entity.setJoinDate(dto.getJoinDate());
        if (dto.getJvDocRef() != null) entity.setJvDocRef(dto.getJvDocRef());
        if (dto.getJvPoid() != null) entity.setJvPoid(dto.getJvPoid());
        if (dto.getLastLeaveDetails() != null) entity.setLastLeaveDetails(dto.getLastLeaveDetails());
        if (dto.getWorkDaysFromLastLeave() != null) entity.setWorkDaysFromLastLeave(dto.getWorkDaysFromLastLeave());
        if (dto.getPaidLeavesTaken() != null) entity.setPaidLeavesTaken(dto.getPaidLeavesTaken());
        if (dto.getAttendance2Poid() != null) entity.setAttendance2Poid(dto.getAttendance2Poid());
        if (dto.getFixedOt() != null) entity.setFixedOt(dto.getFixedOt());
        if (dto.getFixedOt2() != null) entity.setFixedOt2(dto.getFixedOt2());
        if (dto.getBasicFixAlw() != null) entity.setBasicFixAlw(dto.getBasicFixAlw());
        if (dto.getBasicFixOt() != null) entity.setBasicFixOt(dto.getBasicFixOt());
        if (dto.getLastRejoinDate() != null) entity.setLastRejoinDate(dto.getLastRejoinDate());
        if (dto.getBasicEligibleLeaveDays() != null) entity.setBasicEligibleLeaveDays(dto.getBasicEligibleLeaveDays());
        if (dto.getBasicSalPerDay() != null) entity.setBasicSalPerDay(dto.getBasicSalPerDay());
        if (dto.getGrossSalPerDay() != null) entity.setGrossSalPerDay(dto.getGrossSalPerDay());
        if (dto.getBasicHra() != null) entity.setBasicHra(dto.getBasicHra());
        if (dto.getBasicTransport() != null) entity.setBasicTransport(dto.getBasicTransport());
        if (dto.getExgratia() != null) entity.setExgratia(dto.getExgratia());
        if (dto.getExgratiaGl() != null) entity.setExgratiaGl(dto.getExgratiaGl());
        if (dto.getRoundOff() != null) entity.setRoundOff(dto.getRoundOff());
        if (dto.getPaymentDocType() != null) entity.setPaymentDocType(dto.getPaymentDocType());
        if (dto.getPaymentDocRef() != null) entity.setPaymentDocRef(dto.getPaymentDocRef());
        if (dto.getPaymentDocPoid() != null) entity.setPaymentDocPoid(dto.getPaymentDocPoid());
        if (dto.getPaymentValueDate() != null) entity.setPaymentValueDate(dto.getPaymentValueDate());
        if (dto.getPaymentPayeeName() != null) entity.setPaymentPayeeName(dto.getPaymentPayeeName());
        if (dto.getPaymentBankPoid() != null) entity.setPaymentBankPoid(dto.getPaymentBankPoid());
        if (dto.getPaymentPrePrinted() != null) entity.setPaymentPrePrinted(dto.getPaymentPrePrinted());
        if (dto.getCrPoid() != null) entity.setCrPoid(dto.getCrPoid());
        if (dto.getIndemnityFirstThreeYrDays() != null) entity.setIndemnityFirstThreeYrDays(dto.getIndemnityFirstThreeYrDays());
        if (dto.getIndemnityAfterThreeYrDays() != null) entity.setIndemnityAfterThreeYrDays(dto.getIndemnityAfterThreeYrDays());
        if (dto.getIndemnityServiceDays() != null) entity.setIndemnityServiceDays(dto.getIndemnityServiceDays());
        if (dto.getOtherAllowance2() != null) entity.setOtherAllowance2(dto.getOtherAllowance2());
        if (dto.getOtherAllowGl2() != null) entity.setOtherAllowGl2(dto.getOtherAllowGl2());
        if (dto.getOtherAllowanceDetails2() != null) entity.setOtherAllowanceDetails2(dto.getOtherAllowanceDetails2());
        if (dto.getLeaveAbsentDays() != null) entity.setLeaveAbsentDays(dto.getLeaveAbsentDays());
        if (dto.getLeaveWorkDaysStage1() != null) entity.setLeaveWorkDaysStage1(dto.getLeaveWorkDaysStage1());
        if (dto.getLeaveEligibleLeaveStage1() != null) entity.setLeaveEligibleLeaveStage1(dto.getLeaveEligibleLeaveStage1());
        if (dto.getLastTicketDetails() != null) entity.setLastTicketDetails(dto.getLastTicketDetails());
        if (dto.getLateDeductionAmt() != null) entity.setLateDeductionAmt(dto.getLateDeductionAmt());
        if (dto.getTicketsEarned() != null) entity.setTicketsEarned(dto.getTicketsEarned());
        if (dto.getExcessVisaCharges() != null) entity.setExcessVisaCharges(dto.getExcessVisaCharges());
        if (dto.getTicketTillDate() != null) entity.setTicketTillDate(dto.getTicketTillDate());
        if (dto.getIndemnityPaidAmt() != null) entity.setIndemnityPaidAmt(dto.getIndemnityPaidAmt());
        if (dto.getIndemnityWithoutPayDays() != null) entity.setIndemnityWithoutPayDays(dto.getIndemnityWithoutPayDays());
        if (dto.getRecovVisaCost() != null) entity.setRecovVisaCost(dto.getRecovVisaCost());
        if (dto.getRecovGosiCost() != null) entity.setRecovGosiCost(dto.getRecovGosiCost());
        if (dto.getRecovLmraCost() != null) entity.setRecovLmraCost(dto.getRecovLmraCost());
        if (dto.getRecovMdinsCost() != null) entity.setRecovMdinsCost(dto.getRecovMdinsCost());
        if (dto.getRecovTicketCost() != null) entity.setRecovTicketCost(dto.getRecovTicketCost());
        if (dto.getTicketEligiblityTemp() != null) entity.setTicketEligiblityTemp(dto.getTicketEligiblityTemp());
        if (dto.getIndemnityEligibleDays() != null) entity.setIndemnityEligibleDays(dto.getIndemnityEligibleDays());
        if (dto.getSioShareIndemnity() != null) entity.setSioShareIndemnity(dto.getSioShareIndemnity());
        if (dto.getIndmntyMnthsCompany() != null) entity.setIndmntyMnthsCompany(dto.getIndmntyMnthsCompany());
        if (dto.getIndmntyMnthsSio() != null) entity.setIndmntyMnthsSio(dto.getIndmntyMnthsSio());
        if (dto.getIndemnityProvAmount() != null) entity.setIndemnityProvAmount(dto.getIndemnityProvAmount());
    }

    public static LoanDeductionDto mapLoanDtlToDto(LoanDeductionDtl entity) {
        if (entity == null) return null;
        return LoanDeductionDto.builder()
                .detRowId(entity.getDetRowId())
                .transactionPoid(entity.getTransactionPoid())
                .employeePoid(entity.getEmployeePoid())
                .recurType(entity.getRecurType())
                .recurAmount(entity.getRecurAmount())
                .refNo(entity.getRefNo())
                .verified(entity.getVerified())
                .remarks(entity.getRemarks())
                .recurTranPoid(entity.getRecurTranPoid())
                .allowanceDeductionPoid(entity.getAllowanceDeductionPoid())
                .balanceAmt(entity.getBalanceAmt())
                .build();
    }

    public static LoanDeductionDtl mapLoanDtlFromDto(LoanDeductionDto dto, Long transactionPoid) {
        if (dto == null) return null;
        return LoanDeductionDtl.builder()
                .detRowId(dto.getDetRowId())
                .transactionPoid(transactionPoid)
                .employeePoid(dto.getEmployeePoid())
                .recurType(dto.getRecurType())
                .recurAmount(dto.getRecurAmount())
                .refNo(dto.getRefNo())
                .verified(dto.getVerified())
                .remarks(dto.getRemarks())
                .recurTranPoid(dto.getRecurTranPoid())
                .allowanceDeductionPoid(dto.getAllowanceDeductionPoid())
                .balanceAmt(dto.getBalanceAmt())
                .build();
    }

    public static List<LoanDeductionDto> mapLoanDtlListToDto(List<LoanDeductionDtl> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(EmployeeSettlementMapper::mapLoanDtlToDto)
                .collect(Collectors.toList());
    }

    public static List<LoanDeductionDtl> mapLoanDtlListFromDto(List<LoanDeductionDto> dtos, Long transactionPoid) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(dto -> mapLoanDtlFromDto(dto, transactionPoid))
                .collect(Collectors.toList());
    }

}
