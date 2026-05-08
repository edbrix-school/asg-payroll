package com.asg.payroll.employeeappraisal.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HrAppraisalDtlId implements Serializable {
    private Long transactionPoid;
    private Long detRowId;
}