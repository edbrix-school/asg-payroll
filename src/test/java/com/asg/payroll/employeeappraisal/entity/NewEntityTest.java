package com.asg.payroll.employeeappraisal.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NewEntityTest {

    @Test
    void hrPayrollVarAlwdedDtl_Instantiation() {
        assertNotNull(new HrPayrollVarAlwdedDtl());
    }

    @Test
    void hrPayrollVarAlwdedDtlId_Instantiation() {
        assertNotNull(new HrPayrollVarAlwdedDtlId());
    }

    @Test
    void hrEmployeeSalaryMaster_Instantiation() {
        assertNotNull(new HrEmployeeSalaryMaster());
    }
}
