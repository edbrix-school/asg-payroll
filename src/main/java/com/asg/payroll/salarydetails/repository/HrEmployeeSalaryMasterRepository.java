package com.asg.payroll.salarydetails.repository;

import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HrEmployeeSalaryMasterRepository extends JpaRepository<HrEmployeeSalaryMaster, Long> {
    Optional<HrEmployeeSalaryMaster> findByEmployeePoid(Long employeePoid);
}
