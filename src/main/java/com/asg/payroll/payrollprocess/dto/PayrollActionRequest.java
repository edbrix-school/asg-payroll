package com.asg.payroll.payrollprocess.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollActionRequest {

    private Long attendTranPoid;

    private LocalDate payrollMonth;

    private Boolean suppressArrearsValidation;

    private LocalDateTime scheduleOn;

    private String resend;
}
