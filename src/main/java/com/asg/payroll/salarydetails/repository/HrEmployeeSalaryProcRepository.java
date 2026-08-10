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

    /**
     * Returns the CR_NUMBER from ADMIN_CR_MASTER for the given CR poids, keyed by poid.
     */
    Map<Long, String> getCrNumbers(Collection<Long> poids);

    Map<Long, String> getFullEmployeeNamesBySalaryPoids(Collection<Long> salaryPoids);

    String addToSalaryHistory(Long companyId, Long loginUserPoid, Long salaryPoid);

    String syncHRData();

    String calculateCTC(Long groupPoid, Long companyPoid, Long employeePoid);
}
