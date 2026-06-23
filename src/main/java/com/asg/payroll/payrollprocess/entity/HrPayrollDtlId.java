package com.asg.payroll.payrollprocess.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class HrPayrollDtlId implements Serializable {
    private Long detRowId;
    private Long transactionPoid;
}
