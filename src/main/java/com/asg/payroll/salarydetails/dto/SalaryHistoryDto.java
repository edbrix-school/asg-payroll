package com.asg.payroll.salarydetails.dto;

import com.asg.common.lib.dto.LovGetListDto;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryHistoryDto {

    private Long detRowId;
    private LocalDate incrementDate;
    private BigDecimal basicSalary;
    private BigDecimal gosiSalary;
    private BigDecimal fixedAllowance;
    private BigDecimal fixedOt;
    private BigDecimal hra;
    private BigDecimal transport;
    private BigDecimal gross;
    private LovGetListDto designationDet;
    private Long noOfTickets;
    private Long ticketPeriod;
    private String crNumber;
    private String remarks;
}
