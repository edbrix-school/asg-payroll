package com.asg.payroll.payrollprocess.repository;

import com.asg.payroll.payrollprocess.entity.HrPayrollRecurringDtl;
import com.asg.payroll.payrollprocess.entity.HrPayrollRecurringDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrPayrollRecurringDtlRepository extends JpaRepository<HrPayrollRecurringDtl, HrPayrollRecurringDtlId> {

    List<HrPayrollRecurringDtl> findByTransactionPoid(Long transactionPoid);

    @Query("select coalesce(max(d.detRowId), 0) from HrPayrollRecurringDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(Long transactionPoid);

}
