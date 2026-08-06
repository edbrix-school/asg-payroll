package com.asg.payroll.salarydetails.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import java.sql.ResultSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class HrEmployeeSalaryProcRepositoryImpl implements HrEmployeeSalaryProcRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, Object> getEmployeeDetails(Long employeePoid) {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("PROC_HR_GET_EMP_DETAILS");
            // Use positional parameters matching proc signature: (P_EMPLOYEE_POID IN, OUTDATA OUT SYS_REFCURSOR, P_STATUS OUT)
            query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, ResultSet.class, ParameterMode.REF_CURSOR);
            query.registerStoredProcedureParameter(3, String.class, ParameterMode.OUT);

            query.setParameter(1, employeePoid);

            query.execute();

            String status = (String) query.getOutputParameterValue(3);
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
    public Map<Long, String> getAllowanceDeductionTypes(Collection<Long> poids) {
        if (poids == null || poids.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                            "SELECT ALLOWACE_DEDUCTION_POID, TYPE FROM HR_ALLOWANCE_DEDUCTION_MASTER "
                                    + "WHERE ALLOWACE_DEDUCTION_POID IN (:poids)")
                    .setParameter("poids", poids)
                    .getResultList();

            Map<Long, String> typeByPoid = new HashMap<>();
            for (Object[] row : rows) {
                if (row[0] == null) {
                    continue;
                }
                Long poid = ((Number) row[0]).longValue();
                String type = row[1] != null ? row[1].toString() : null;
                typeByPoid.put(poid, type);
            }
            return typeByPoid;
        } catch (Exception e) {
            log.error("Error fetching allowance/deduction types: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<Long, String> getCrNumbers(Collection<Long> poids) {
        if (poids == null || poids.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                            "SELECT CR_POID, CR_NUMBER FROM ADMIN_CR_MASTER WHERE CR_POID IN (:poids)")
                    .setParameter("poids", poids)
                    .getResultList();

            Map<Long, String> crNumberByPoid = new HashMap<>();
            for (Object[] row : rows) {
                if (row[0] == null) {
                    continue;
                }
                Long poid = ((Number) row[0]).longValue();
                String crNumber = row[1] != null ? row[1].toString() : null;
                crNumberByPoid.put(poid, crNumber);
            }
            return crNumberByPoid;
        } catch (Exception e) {
            log.error("Error fetching CR numbers: {}", e.getMessage());
            return Collections.emptyMap();
        }
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
        if (employeePoid == null) {
            return "0";
        }
        try {
            Long gPoid = (groupPoid != null && groupPoid > 0) ? groupPoid : (companyPoid != null && companyPoid > 0 ? companyPoid : 1L);
            Long cPoid = (companyPoid != null && companyPoid > 0) ? companyPoid : gPoid;

            Object result = entityManager.createNativeQuery("SELECT FUNC_EMPLOYEE_RPT_CTC(?, ?, ?) FROM DUAL")
                    .setParameter(1, gPoid)
                    .setParameter(2, cPoid)
                    .setParameter(3, employeePoid)
                    .getSingleResult();
            return result != null ? result.toString().trim() : "0";
        } catch (Exception e) {
            log.error("Error calling FUNC_EMPLOYEE_RPT_CTC: {}", e.getMessage());
            return "0";
        }
    }
}
