package com.asg.payroll.salarydetails.repository;

import java.util.Collection;
import java.util.Map;

public interface HrEmployeeSalaryProcRepository {

    Map<String, Object> getEmployeeDetails(Long employeePoid);

    /**
     * Returns the allowance/deduction TYPE (e.g. ALLOWANCE, DEDUCTION, PROVISION)
     * from HR_ALLOWANCE_DEDUCTION_MASTER for the given poids, keyed by poid.
     */
    Map<Long, String> getAllowanceDeductionTypes(Collection<Long> poids);

    String addToSalaryHistory(Long companyId, Long loginUserPoid, Long salaryPoid);

    String syncHRData();

    String calculateCTC(Long groupPoid, Long companyPoid, Long employeePoid);
}
