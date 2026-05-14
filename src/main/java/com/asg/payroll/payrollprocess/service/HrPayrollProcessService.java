package com.asg.payroll.payrollprocess.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.payroll.payrollprocess.dto.HrPayrollHdrRequest;
import com.asg.payroll.payrollprocess.dto.PayrollActionRequest;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface HrPayrollProcessService {

    Map<String, Object> listPayrolls(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    Map<String, Object> getPayrollById(Long transactionPoid);

    Map<String, Object> createPayroll(HrPayrollHdrRequest request);

    Map<String, Object> updatePayroll(Long transactionPoid, HrPayrollHdrRequest request);

    void deletePayroll(Long transactionPoid, DeleteReasonDto deleteReasonDto);

    Map<String, Object> processPayroll(Long transactionPoid, PayrollActionRequest request);

    Map<String, Object> processProvision(Long transactionPoid, PayrollActionRequest request);

    Map<String, Object> revertPayroll(Long transactionPoid);

    Map<String, Object> loadVariables(Long transactionPoid, PayrollActionRequest request);

    Map<String, Object> loadLoansAdvances(Long transactionPoid, PayrollActionRequest request);

    Map<String, Object> createJv(Long transactionPoid);

    Map<String, Object> generateBankFile(Long transactionPoid);

    Map<String, Object> hsbcApiTransfer(Long transactionPoid);

    Map<String, Object> syncHrData();

    Map<String, Object> sendEmail(Long transactionPoid, PayrollActionRequest request);

    byte[] printPayslip(Long transactionPoid) throws JRException;

    byte[] printPreview(Long transactionPoid) throws JRException;
}
