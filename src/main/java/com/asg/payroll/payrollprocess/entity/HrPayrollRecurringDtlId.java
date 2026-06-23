package com.asg.payroll.payrollprocess.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class HrPayrollRecurringDtlId implements Serializable {
    private Long transactionPoid;
    private Long detRowId;
}
