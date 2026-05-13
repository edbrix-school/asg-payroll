package com.asg.payroll.salarydetails.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@Slf4j
public class HrEmployeeSalaryProcRepositoryImpl implements HrEmployeeSalaryProcRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, Object> getEmployeeDetails(Long employeePoid) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("PROC_HR_GET_EMP_DETAILS");
            query.registerStoredProcedureParameter("P_EMPLOYEE_POID", Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("OUTDATA", void.class, ParameterMode.REF_CURSOR);
            query.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT);

            query.setParameter("P_EMPLOYEE_POID", employeePoid);

            query.execute();

            String status = (String) query.getOutputParameterValue("P_STATUS");
            if (!"SUCCESS".equalsIgnoreCase(status)) {
                log.error("PROC_HR_GET_EMP_DETAILS failed with status: {}", status);
                return null;
            }

            List<Object[]> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                Object[] row = results.get(0);
                Map<String, Object> details = new HashMap<>();
                details.put("EMPLOYEE_POID", row[0]);
                details.put("TICKET_DETAILS", row[1]);
                details.put("DESIGNATION_NAME", row[2]);
                details.put("JOIN_DATE", row[3]);
                details.put("PROBATION", row[4]);

                return details;
            }
        } catch (Exception e) {
            log.error("Error executing PROC_HR_GET_EMP_DETAILS: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String addToSalaryHistory(Long companyId, Long loginUserPoid, Long salaryPoid) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("PROC_HR_SALARY_TO_HISTORY");
            query.registerStoredProcedureParameter("P_COMPANYID", Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("P_LOGIN_USER_POID", Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("P_SALARY_POID", Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT);

            query.setParameter("P_COMPANYID", companyId);
            query.setParameter("P_LOGIN_USER_POID", loginUserPoid);
            query.setParameter("P_SALARY_POID", salaryPoid);

            query.execute();

            return (String) query.getOutputParameterValue("P_STATUS");
        } catch (Exception e) {
            log.error("Error executing PROC_HR_SALARY_TO_HISTORY: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String syncHRData() {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("SYNC_HR_PRODUCTION_TO_PAYROLL");
            query.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT);
            query.execute();
            return (String) query.getOutputParameterValue("P_STATUS");
        } catch (Exception e) {
            log.error("Error executing SYNC_HR_PRODUCTION_TO_PAYROLL: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public String calculateCTC(Long groupPoid, Long companyPoid, Long employeePoid) {
        try {
            // FUNC_EMPLOYEE_RPT_CTC is a function, not a procedure
            // Using native query to call function
            Object result = entityManager.createNativeQuery("SELECT FUNC_EMPLOYEE_RPT_CTC(?, ?, ?) FROM DUAL")
                    .setParameter(1, groupPoid)
                    .setParameter(2, companyPoid)
                    .setParameter(3, employeePoid)
                    .getSingleResult();
            return result != null ? result.toString() : "0";
        } catch (Exception e) {
            log.error("Error calling FUNC_EMPLOYEE_RPT_CTC: {}", e.getMessage());
            return "0";
        }
    }
}
