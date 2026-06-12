package com.asg.payroll.employeeSettlement.repository;

import com.asg.payroll.employeeSettlement.entity.LoanDeductionDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanDeductionDtlRepository extends JpaRepository<LoanDeductionDtl, Long> {

    List<LoanDeductionDtl> findByTransactionPoidOrderByDetRowId(Long transactionPoid);

    void deleteByTransactionPoid(Long transactionPoid);
}
