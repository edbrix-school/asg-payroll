package com.asg.payroll.payrollprocess.repository;

import com.asg.payroll.payrollprocess.entity.HrPayrollProvisionDtl;
import com.asg.payroll.payrollprocess.entity.HrPayrollProvisionDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrPayrollProvisionDtlRepository extends JpaRepository<HrPayrollProvisionDtl, HrPayrollProvisionDtlId> {

    List<HrPayrollProvisionDtl> findByTransactionPoid(Long transactionPoid);
}
