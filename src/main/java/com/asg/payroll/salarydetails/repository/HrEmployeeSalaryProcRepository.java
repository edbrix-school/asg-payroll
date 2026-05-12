package com.asg.payroll.salarydetails.repository;

import java.util.Map;

public interface HrEmployeeSalaryProcRepository {

    Map<String, Object> getEmployeeDetails(Long employeePoid);

    String addToSalaryHistory(Long companyId, Long loginUserPoid, Long salaryPoid);

    String syncHRData();

    String calculateCTC(Long groupPoid, Long companyPoid, Long employeePoid);
}
