package com.asg.payroll.salarydetails.repository;

import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HrEmployeeSalaryMasterRepository extends JpaRepository<HrEmployeeSalaryMaster, Long> {

    boolean existsByEmployeePoid(Long employeePoid);

    Optional<HrEmployeeSalaryMaster> findByEmployeePoid(Long employeePoid);
}
