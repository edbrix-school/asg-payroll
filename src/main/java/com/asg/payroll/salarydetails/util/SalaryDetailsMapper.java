package com.asg.payroll.salarydetails.util;

import com.asg.payroll.salarydetails.dto.SalaryAllowanceDto;
import com.asg.payroll.salarydetails.dto.SalaryDetailRequest;
import com.asg.payroll.salarydetails.dto.SalaryDetailResponse;
import com.asg.payroll.salarydetails.dto.SalaryHistoryDto;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryAlwDtl;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryHist;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryMaster;

import java.util.List;

public class SalaryDetailsMapper {

    private SalaryDetailsMapper() {
    }

    public static void mapToEntity(SalaryDetailRequest request, HrEmployeeSalaryMaster entity) {
        if (request == null) return;

        entity.setEmployeePoid(request.getEmployeePoid());
        entity.setLastIncrementDate(request.getLastIncrementDate());
        entity.setNextIncrementDate(request.getNextIncrementDate());
        entity.setIndemnityDueFrom(request.getIndemnityDueFrom());
        entity.setBasicSalary(request.getBasicSalary());
        entity.setRegisteredSalary(request.getRegisteredSalary());
        entity.setGosiType(request.getGosiType());
        entity.setPaymentMethod(request.getPaymentMethod());
        entity.setBankPoid(request.getBankPoid());
        entity.setIbanAccountNo(request.getIbanAccountNo());
        entity.setBankRegistrationId(request.getBankRegistrationId());
        entity.setAccountName(request.getAccountName());
        entity.setBankGuarantee(request.getBankGuarantee());
        entity.setBankGuaranteeDetails(request.getBankGuaranteeDetails());
        entity.setBankGuaranteeDueDate(request.getBankGuaranteeDueDate());
        entity.setContractPrintType(request.getContractPrintType());
        entity.setIndemnityPaidAmt(request.getIndemnityPaidAmt());
        entity.setIndemnityPaidDet(request.getIndemnityPaidDet());
        entity.setLoanDeductionAmt(request.getLoanDeductionAmt() != null ? request.getLoanDeductionAmt() : 0L);
        entity.setAccommodationCost(request.getAccommodationCost());
        entity.setRemarks(request.getRemarks());
    }

    public static SalaryDetailResponse mapToResponse(HrEmployeeSalaryMaster entity, List<HrEmployeeSalaryAlwDtl> allowances, List<HrEmployeeSalaryHist> history) {
        if (entity == null) return null;

        SalaryDetailResponse response = SalaryDetailResponse.builder()
                .salaryPoid(entity.getSalaryPoid())
                .employeePoid(entity.getEmployeePoid())
                .lastIncrementDate(entity.getLastIncrementDate())
                .nextIncrementDate(entity.getNextIncrementDate())
                .indemnityDueFrom(entity.getIndemnityDueFrom())
                .basicSalary(entity.getBasicSalary())
                .registeredSalary(entity.getRegisteredSalary())
                .gosiType(entity.getGosiType())
                .grossSalary(entity.getGrossSalary())
                .netSalary(entity.getNetSalary())
                .totalAllowance(entity.getTotalAllowance())
                .paymentMethod(entity.getPaymentMethod())
                .bankPoid(entity.getBankPoid())
                .ibanAccountNo(entity.getIbanAccountNo())
                .bankRegistrationId(entity.getBankRegistrationId())
                .accountName(entity.getAccountName())
                .bankGuarantee(entity.getBankGuarantee())
                .bankGuaranteeDetails(entity.getBankGuaranteeDetails())
                .bankGuaranteeDueDate(entity.getBankGuaranteeDueDate())
                .contractPrintType(entity.getContractPrintType())
                .indemnityPaidAmt(entity.getIndemnityPaidAmt())
                .indemnityPaidDet(entity.getIndemnityPaidDet())
                .loanDeductionAmt(entity.getLoanDeductionAmt())
                .accommodationCost(entity.getAccommodationCost())
                .remarks(entity.getRemarks())
                .build();

        if (allowances != null) {
            response.setAllowances(allowances.stream().map(SalaryDetailsMapper::mapToAllowanceDto).toList());
        }

        if (history != null) {
            response.setHistory(history.stream().map(SalaryDetailsMapper::mapToHistoryDto).toList());
        }

        return response;
    }

    public static SalaryAllowanceDto mapToAllowanceDto(HrEmployeeSalaryAlwDtl entity) {
        if (entity == null) return null;
        return SalaryAllowanceDto.builder()
                .allowanceDeductionPoid(entity.getAllowanceDeductionPoid())
                .amount(entity.getAmount())
                .active(entity.getActive())
                .formula(entity.getFormula())
                .remarks(entity.getRemarks())
                .build();
    }

    public static SalaryHistoryDto mapToHistoryDto(HrEmployeeSalaryHist entity) {
        if (entity == null) return null;
        return SalaryHistoryDto.builder()
                .detRowId(entity.getDetRowId() != null ? entity.getDetRowId() : null)
                .incrementDate(entity.getLastIncrementDate())
                .basicSalary(entity.getBasicSalary())
                .gosiSalary(entity.getRegisteredSalary())
                .fixedAllowance(entity.getFaAlw())
                .fixedOt(entity.getFixotAlw())
                .hra(entity.getHraAlw())
                .transport(entity.getTaAlw())
                .gross(entity.getGrossPay())
                .designation(String.valueOf(entity.getDesignationPoid())) // Name needs to be fetched or already present if we change DTO
                .noOfTickets(entity.getNoOfTickets())
                .ticketPeriod(entity.getTicketPeriod())
                .remarks(entity.getRemarks())
                .build();
    }

    public static HrEmployeeSalaryAlwDtl mapDtoToAllowanceEntity(SalaryAllowanceDto dto, HrEmployeeSalaryAlwDtl entity, Long salaryPoid) {
        if (dto == null || entity == null) return null;
        entity.setAllowanceDeductionPoid(dto.getAllowanceDeductionPoid());
        entity.setAmount(dto.getAmount());
        entity.setActive(dto.getActive());
        entity.setFormula(dto.getFormula());
        entity.setRemarks(dto.getRemarks());
        entity.setSalaryPoid(salaryPoid);
        entity.setDetRowId(dto.getDetRowId());
        return entity;
    }
}
