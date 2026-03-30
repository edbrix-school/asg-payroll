package com.asg.payroll.salarydetails.entity;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HrEmployeeSalaryHistId implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Column(name = "SALARY_POID")
    private Long salaryPoid;

    @Column(name = "DET_ROW_ID")
    private Long detRowId;
}
