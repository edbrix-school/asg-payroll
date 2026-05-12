package com.asg.payroll.employeeappraisal.repository;

import com.asg.payroll.employeeappraisal.entity.HrEmployeeSalaryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HrEmployeeSalaryMasterRepository extends JpaRepository<HrEmployeeSalaryMaster, Long> {

    boolean existsByEmployeePoid(Long employeePoid);
}
