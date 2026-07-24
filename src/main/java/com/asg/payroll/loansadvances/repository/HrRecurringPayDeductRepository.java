package com.asg.payroll.loansadvances.repository;

import com.asg.payroll.loansadvances.entity.HrRecurringPayDeduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HrRecurringPayDeductRepository extends JpaRepository<HrRecurringPayDeduct, Long> {


    Optional<HrRecurringPayDeduct> findByTransactionPoid(Long id);


}
