package com.asg.payroll.payrollprocess.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariableLoadResponse {
    
    private String status;
    private List<Map<String, Object>> variables;
}