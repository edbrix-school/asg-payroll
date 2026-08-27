package com.asg.payroll.employeeappraisal.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalActionRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRecalculationRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRequest;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface HrAppraisalService {
    Map<String, Object> listAppraisals(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    Map<String, Object> getAppraisalById(Long transactionPoid);

    Map<String, Object> getFilteredDetails(Long transactionPoid, Long departmentPoid, Long designationPoid, String listingMethod, String employeeName);

    Map<String, Object> createAppraisal(HrAppraisalRequest request);

    Map<String, Object> updateAppraisal(Long transactionPoid, HrAppraisalRequest request);

    void deleteAppraisal(Long transactionPoid, DeleteReasonDto deleteReasonDto);

    Map<String, Object> getDetailsSp(Long transactionPoid, Long employeePoid);

    Map<String, Object> loadAppraisalDataSp(Long transactionPoid, String actionType);

    Map<String, Object> clearAppraisalDataSp(Long transactionPoid);

    Map<String, Object> batchUpdateSp(Long transactionPoid, HrAppraisalActionRequest request);

    Map<String, Object> recalculateDetail(HrAppraisalRecalculationRequest request);

    Map<String, Object> updateDataSp(Long transactionPoid, Long employeePoid, HrAppraisalActionRequest request);

    Map<String, Object> updateMasterSp(Long transactionPoid, HrAppraisalActionRequest request);

    Map<String, Object> createJvSp(Long transactionPoid);

    Map<String, Object> sendEmailSp(Long transactionPoid, String resend);

    /** Generates the appraisal bank transfer file and returns its contents for download. */
    byte[] bankFileSp(Long transactionPoid);

    Map<String, Object> arrearsSp(Long transactionPoid, Long payrollPoid);

    Map<String, Object> arrearsCalcSp(Long transactionPoid);

    byte[] printA3(Long transactionPoid) throws JRException;

    byte[] printByCompany(Long transactionPoid) throws JRException;

    byte[] printBank(Long transactionPoid) throws JRException;

    byte[] printLetter(Long transactionPoid, Long employeePoid) throws JRException;
}
