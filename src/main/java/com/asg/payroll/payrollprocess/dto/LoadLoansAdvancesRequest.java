package com.asg.payroll.payrollprocess.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoadLoansAdvancesRequest {
    private Long settlementPoid;
    private Long empPoid;
    private LocalDate payrollDate;
}