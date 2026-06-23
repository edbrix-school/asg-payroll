package com.asg.payroll.payrollprocess.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JvCreationResponse {
    
    private String payrollJvStatus;
    private String provisionJvStatus;
    private String bankDvStatus;
}