package com.asg.payroll.salarydetails.repository;

import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryHist;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryHistId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrEmployeeSalaryHistRepository extends JpaRepository<HrEmployeeSalaryHist, HrEmployeeSalaryHistId> {
    List<HrEmployeeSalaryHist> findBySalaryPoid(Long salaryPoid);
}
