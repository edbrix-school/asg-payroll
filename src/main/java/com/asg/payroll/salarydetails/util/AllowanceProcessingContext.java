package com.asg.payroll.salarydetails.util;

import com.asg.common.lib.dto.request.LogRequestDto;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryAlwDtl;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class AllowanceProcessingContext {
    private Long salaryPoid;
    private Long maxDetRowId;
    private String docId;
    private String docKeyPoid;

    private List<HrEmployeeSalaryAlwDtl> toSave;
    private List<HrEmployeeSalaryAlwDtl> toUpdate;
    private List<Long> toDelete;
    private List<LogRequestDto<HrEmployeeSalaryAlwDtl>> logRequests;
}
