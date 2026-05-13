package com.asg.payroll.loansadvances.repository;

import com.asg.payroll.loansadvances.entity.HrRecurringPayDeduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HrRecurringPayDeductRepository extends JpaRepository<HrRecurringPayDeduct, Long> {

    @Query("""
            SELECT h 
            FROM HrRecurringPayDeduct h
            WHERE h.transactionPoid = :id
            AND (h.deleted = 'N' OR h.deleted IS NULL)
            """)
    Optional<HrRecurringPayDeduct> findByTransactionPoidDeleted(Long id);


}
