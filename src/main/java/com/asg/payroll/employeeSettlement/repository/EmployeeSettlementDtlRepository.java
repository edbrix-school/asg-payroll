package com.asg.payroll.employeeSettlement.repository;

import com.asg.payroll.employeeSettlement.entity.EmployeeSettlementDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface EmployeeSettlementDtlRepository extends JpaRepository<EmployeeSettlementDtl,Long> {

    Optional<EmployeeSettlementDtl> findByTransactionPoid(Long trasactionPoid);
}
