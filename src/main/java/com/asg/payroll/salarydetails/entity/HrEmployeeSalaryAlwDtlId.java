package com.asg.payroll.salarydetails.entity;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class HrEmployeeSalaryAlwDtlId implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Column(name = "SALARY_POID")
    private Long salaryPoid;

    @Column(name = "DET_ROW_ID")
    private Long detRowId;
}