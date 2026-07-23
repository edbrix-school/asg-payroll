package com.asg.payroll.payrollprocess.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrPayrollHdrRequest {

    private LocalDate transactionDate;

    @NotNull(message = "Attendance period is required.")
    private Long attendTranPoid;

    @NotNull(message = "Payroll month is required.")
    private LocalDate payrollMonth;

    private String attendancePeriodDesc;

    private LocalDate bankTransferValueDate;

    private LocalDateTime emailPayslipScheduleOn;

    private String suppressArrearsValidation;

    @Valid
    private List<HrPayrollVarAlwdedDtlRequest> variableDetails;

    @Valid
    private List<HrPayrollRecurringDtlRequest> recurringDetails;
}
