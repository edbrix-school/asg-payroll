package com.asg.payroll.payrollprocess.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoansAdvancesResponse {
    
    private List<Map<String, Object>> loansAdvances;
}