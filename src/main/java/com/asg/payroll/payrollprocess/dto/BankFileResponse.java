package com.asg.payroll.payrollprocess.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankFileResponse {
    
    private String fileName;
    private String status;
}