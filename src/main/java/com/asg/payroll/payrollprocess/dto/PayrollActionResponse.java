package com.asg.payroll.payrollprocess.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollActionResponse {
    
    private String status;
    private String message;
}