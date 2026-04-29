package com.asg.payroll.employeeappraisal.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class HrPayrollVarAlwdedDtlId implements Serializable {
    private Long detRowId;
    private Long transactionPoid;
}
