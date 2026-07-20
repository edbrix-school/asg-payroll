package com.asg.payroll.salarydetails.dto;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryHistoryDto {

    private Long detRowId;
    private LocalDate incrementDate;
    private Long basicSalary;
    private Long gosiSalary;
    private Long fixedAllowance;
    private Long fixedOt;
    private Long hra;
    private Long transport;
    private Long gross;
    private LovGetListDto designationDet;
    private Long noOfTickets;
    private Long ticketPeriod;
    private String crNumber;
    private String remarks;
}
