package com.asg.payroll.payrollprocess.repository;

import com.asg.payroll.payrollprocess.entity.HrPayrollDtl;
import com.asg.payroll.payrollprocess.entity.HrPayrollDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrPayrollDtlRepository extends JpaRepository<HrPayrollDtl, HrPayrollDtlId> {

    List<HrPayrollDtl> findByTransactionPoid(Long transactionPoid);

    @Query("select coalesce(max(d.detRowId), 0) from HrPayrollDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(Long transactionPoid);
}
