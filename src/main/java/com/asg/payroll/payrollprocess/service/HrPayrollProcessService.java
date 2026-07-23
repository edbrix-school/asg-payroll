package com.asg.payroll.payrollprocess.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.payroll.payrollprocess.dto.*;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface HrPayrollProcessService {

    Map<String, Object> listPayrolls(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    HrPayrollHdrResponse getPayrollById(Long transactionPoid);

    PayrollMonthValidationResponse validatePayrollMonth(LocalDate payrollMonth, Long transactionPoid);

    HrPayrollHdrResponse createPayroll(HrPayrollHdrRequest request);

    HrPayrollHdrResponse updatePayroll(Long transactionPoid, HrPayrollHdrRequest request);

    void deletePayroll(Long transactionPoid, DeleteReasonDto deleteReasonDto);

    PayrollActionResponse processPayroll(Long transactionPoid, PayrollActionRequest request);

    PayrollActionResponse processProvision(Long transactionPoid, PayrollActionRequest request, String postJv);

    PayrollActionResponse revertPayroll(Long transactionPoid);

    VariableLoadResponse loadVariables(Long transactionPoid, Long settlementPoid, Long empPoid, String payrollDate);

    LoansAdvancesResponse loadLoansAdvances(Long transactionPoid, Long settlementPoid, Long empPoid, LocalDate payrollDate);

    JvCreationResponse createJv(Long userPoid, Long transactionPoid, String bankCash);

    BankFileResponse generateBankFile(Long transactionPoid);

    BankFileResponse hsbcApiTransfer(Long transactionPoid);

    PayrollActionResponse syncHrData();

    PayrollActionResponse sendEmail(Long transactionPoid, PayrollActionRequest request);

    byte[] printPayslip(Long transactionPoid) throws JRException;

    byte[] printPreview(Long transactionPoid) throws JRException;
}
