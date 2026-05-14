package com.asg.payroll.employeeappraisal.repository;

import com.asg.payroll.employeeappraisal.entity.HrPayrollVarAlwdedDtl;
import com.asg.payroll.employeeappraisal.entity.HrPayrollVarAlwdedDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrPayrollVarAlwdedDtlRepository extends JpaRepository<HrPayrollVarAlwdedDtl, HrPayrollVarAlwdedDtlId> {

    List<HrPayrollVarAlwdedDtl> findByTransactionPoid(Long transactionPoid);

    @Query("select coalesce(max(d.detRowId), 0) from HrPayrollVarAlwdedDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(Long transactionPoid);
}
