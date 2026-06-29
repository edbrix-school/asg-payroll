package com.asg.payroll.employeeSettlement.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.payroll.employeeSettlement.dto.EmployeeSettlementDto;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface EmployeeSettlementService {

    EmployeeSettlementDto getEmployeeSettlement(Long id);

    EmployeeSettlementDto createEmployeeSettlement(EmployeeSettlementDto dto);

    void deleteEmployeeSettlement(Long id, DeleteReasonDto deleteReasonDto);

    EmployeeSettlementDto updateEmployeeSettlement(Long id, EmployeeSettlementDto dto);

    Map<String, Object> searchEmployeeSettlement(String docId, FilterRequestDto request, Pageable pageable, LocalDate startDate, LocalDate endDate);

    Object getEmployeeEligibleLeave(Long id);

    String createSettlementBpv(Long id,String paymentMethod, Long paymentBankPoid, String paymentPayeeName, LocalDate paymentValueDate, String paymentPrePrinted);
    String createSettlementBdv(Long id,String paymentMethod, Long paymentBankPoid, String paymentPayeeName, LocalDate paymentValueDate, String paymentPrePrinted);

    String createSettlementJv(Long id);

    String syncHRData();

    Map<String, String> getEmployeeLeaveDates(String employeePoid);


    Map<String, Object> calculateIndemnity(Long companyPoid, Long settlementPoid, Long employeePoid, LocalDate settlementDate, Long withoutPayDays);

    Map<String, Object> getLeaveRequestDetails(Long id);

    Map<String, Object> getRecurringToPayroll(Long payrollPoid, Long settlementPoid, Long empPoid, LocalDate payrollDate);

    Map<String, Object> processLeavePayroll(Long companyPoid, Long settlementTranPoid, Long attendTrnsPoid, Long attend2TrnsPoid, Long empPoid, LocalDate finalDateOfWork, LocalDate leaveEndDate, Long loanDedAmt);

    byte[] printSettlement(Long id) throws Exception;

    byte[] printSettlementAmtDetailsForBank(Long id) throws Exception;

    byte[] printSettlementRetirementLetterForBank(Long id) throws Exception;
}
