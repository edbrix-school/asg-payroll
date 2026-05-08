package com.asg.payroll.employeeappraisal.repository;

import com.asg.payroll.employeeappraisal.entity.HrAppraisalDtl;
import com.asg.payroll.employeeappraisal.entity.HrAppraisalDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HrAppraisalDtlRepository extends JpaRepository<HrAppraisalDtl, HrAppraisalDtlId> {
    List<HrAppraisalDtl> findByTransactionPoid(Long transactionPoid);

    List<HrAppraisalDtl> findByTransactionPoidAndEmployeePoid(Long transactionPoid, Long employeePoid);

    @Query("select coalesce(max(d.detRowId), 0) from HrAppraisalDtl d where d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(Long transactionPoid);
}