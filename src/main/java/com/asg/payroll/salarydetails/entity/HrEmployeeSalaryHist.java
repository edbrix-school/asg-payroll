package com.asg.payroll.salarydetails.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "HR_EMPLOYEE_SALARY_HIST")
@Getter
@Setter
@Builder
@IdClass(HrEmployeeSalaryHistId.class)
@NoArgsConstructor
@AllArgsConstructor
public class HrEmployeeSalaryHist extends BaseEntity {

    @Id
    @Column(name = "SALARY_POID")
    private Long salaryPoid;

    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "LAST_INCREMENT_DATE")
    private LocalDate lastIncrementDate;

    @Column(name = "BASIC_SALARY")
    private BigDecimal basicSalary;

    @Column(name = "REGISTERED_SALARY")
    private BigDecimal registeredSalary;

    @Column(name = "FA_ALW")
    private BigDecimal faAlw;

    @Column(name = "TA_ALW")
    private BigDecimal taAlw;

    @Column(name = "HRA_ALW")
    private BigDecimal hraAlw;

    @Column(name = "FIXOT_ALW")
    private BigDecimal fixotAlw;

    @Column(name = "SPL_ALW")
    private BigDecimal splAlw;

    @Column(name = "GROSS_PAY")
    private BigDecimal grossPay;

    @Column(name = "REMARKS", length = 100)
    private String remarks;

    @Column(name = "DESIGNATION_POID")
    private Long designationPoid;

    @Column(name = "CR_POID")
    private Long crPoid;

    @Column(name = "TICKET_PERIOD")
    private Long ticketPeriod;

    @Column(name = "NO_OF_TICKETS")
    private Long noOfTickets;

}
