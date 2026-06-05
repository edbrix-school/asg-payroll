package com.asg.payroll.payrollprocess.dto;

import lombok.Data;

@Data
public class LoadVariablesRequest {
    private Long settlementPoid;
    private Long empPoid;
    private String payrollDate;
}