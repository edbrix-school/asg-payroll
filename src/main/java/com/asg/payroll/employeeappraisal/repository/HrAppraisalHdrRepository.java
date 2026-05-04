package com.asg.payroll.employeeappraisal.repository;

import com.asg.payroll.employeeappraisal.entity.HrAppraisalHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HrAppraisalHdrRepository extends JpaRepository<HrAppraisalHdr, Long> {
    Optional<HrAppraisalHdr> findByDocRef(String docRef);
}