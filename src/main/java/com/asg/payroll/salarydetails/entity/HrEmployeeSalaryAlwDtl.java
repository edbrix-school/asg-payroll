package com.asg.payroll.salarydetails.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "HR_EMPLOYEE_SALARY_ALW_DTL")
@Getter
@Setter
@Builder
@IdClass(HrEmployeeSalaryAlwDtlId.class)
@NoArgsConstructor
@AllArgsConstructor
public class HrEmployeeSalaryAlwDtl extends BaseEntity {

    @Id
    @Column(name = "SALARY_POID")
    private Long salaryPoid;

    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "ALLOWANCE_DEDUCTION_POID", nullable = false)
    private Long allowanceDeductionPoid;

    @Column(name = "AMOUNT", precision = 20, scale = 3)
    private BigDecimal amount;

    @Column(name = "ACTIVE", length =1)
    private Long active;

    @Column(name = "FORMULA", length = 50)
    private String formula;

    @Column(name = "REMARKS", length = 200)
    private String remarks;
}
