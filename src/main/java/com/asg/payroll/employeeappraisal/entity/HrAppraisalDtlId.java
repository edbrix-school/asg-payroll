package com.asg.payroll.employeeappraisal.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class HrAppraisalDtlId implements Serializable {
    private Long transactionPoid;
    private Long detRowId;
}