package com.asg.payroll.loansadvances.repository;

import com.asg.common.lib.exception.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
@Slf4j
public class LoansAdvancesProcRepositoryImpl implements LoansAdvancesProcRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String PROC_NAME = "PROC_HR_RECURRING_AMT";
    private static final String P_EMP_POID = "P_EMP_POID";
    private static final String P_MON_AMT = "P_MON_AMT";
    private static final String P_RECTYPE = "P_RECTYPE";
    private static final String P_RETURNS = "P_RETURNS";

    @Override
    public String validateRecurring(Long empPoid, BigDecimal monthlyAmt, String recurType) {

        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery(PROC_NAME);

            query.registerStoredProcedureParameter(P_EMP_POID, Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(P_MON_AMT, Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(P_RECTYPE, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(P_RETURNS, String.class, ParameterMode.OUT);

            query.setParameter(P_EMP_POID, empPoid);
            query.setParameter(P_MON_AMT, monthlyAmt);
            query.setParameter(P_RECTYPE, recurType);

            query.execute();

            String status = (String) query.getOutputParameterValue(P_RETURNS);
            log.info("Validation Recurring status: {}", status);

            return status;
        } catch (Exception e) {
            log.error("Error validating Recurring: {}", e.getMessage(), e);
            throw new ValidationException("Failed to validate Recurring: " + e.getMessage());
        }
    }
}
