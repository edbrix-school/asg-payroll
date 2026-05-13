package com.asg.payroll.payrollvariables.repository;

import com.asg.payroll.payrollvariables.entity.HrPayrollVariablesHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollVariablesRepository extends JpaRepository<HrPayrollVariablesHdr, Long> {
    Optional<HrPayrollVariablesHdr> findByTransactionPoid(Long transactionPoid);
    boolean existsByTransactionPoid(Long transactionPoid);
}
