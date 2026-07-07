package com.asg.payroll.payrollprocess.dto;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollHdrResponse {

    private Long transactionPoid;
    private String docRef;
    private LocalDate transactionDate;
    private Long attendTranPoid;
    private LocalDate payrollMonth;
    private String attendancePeriodDesc;
    private String remarks;
    private String suppressArrearsValidation;
    private String payrollJvDocRef;
    private String provJvDocRef;
    private String bankTransferDocRef;
    private LocalDate bankTransferValueDate;
    private String hsbcApiTransfer;
    private LocalDateTime emailPayslipScheduleOn;
    private String emailPayslip;
    private LocalDateTime emailPayslipCompletedOn;
    private BigDecimal totalNetSal;
    private String verified;
    private String approved;
    private String payrollReleased;

    // Edit permission driven by PROC_HR_PAYROLL_VALIDATE (BEFORE_EDIT)
    private Boolean allowEdit;
    private String infoMessage;

    private List<HrPayrollDtlResponse> payrollDetails;
    private List<HrPayrollVarAlwdedDtlResponse> variableDetails;
    private List<HrPayrollProvisionDtlResponse> provisionDetails;
    private List<HrPayrollRecurringDtlResponse> recurringDetails;
    
    // LOV fields
    private LovGetListDto attendancePeriodLov;
    private Map<Long, LovGetListDto> employeeLov;
    private Map<Long, LovGetListDto> allowanceDeductionLov;
}