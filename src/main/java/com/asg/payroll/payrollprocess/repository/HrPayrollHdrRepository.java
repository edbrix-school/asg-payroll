package com.asg.payroll.payrollprocess.repository;

import com.asg.payroll.payrollprocess.entity.HrPayrollHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HrPayrollHdrRepository extends JpaRepository<HrPayrollHdr, Long> {
}
