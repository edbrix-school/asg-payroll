package com.asg.payroll.salarydetails.repository;

import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryAlwDtl;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryAlwDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HrEmployeeSalaryAlwDtlRepository extends JpaRepository<HrEmployeeSalaryAlwDtl, HrEmployeeSalaryAlwDtlId> {
    List<HrEmployeeSalaryAlwDtl> findBySalaryPoid(Long salaryPoid);

    @Query("SELECT COALESCE(MAX(d.detRowId), 0) FROM HrEmployeeSalaryAlwDtl d WHERE d.salaryPoid = :salaryPoid")
    Long getMaxDetRowId(@Param("salaryPoid") Long salaryPoid);

    Optional<HrEmployeeSalaryAlwDtl> findBySalaryPoidAndDetRowId(Long salaryPoid, Long detRowId);

    void deleteBySalaryPoidAndDetRowIdIn(Long salaryPoid, List<Long> detRowIds);
}
