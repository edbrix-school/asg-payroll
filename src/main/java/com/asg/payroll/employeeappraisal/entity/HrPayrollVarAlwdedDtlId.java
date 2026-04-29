package com.asg.payroll.employeeappraisal.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class HrPayrollVarAlwdedDtlId implements Serializable {
    private Long detRowId;
    private Long transactionPoid;
}
