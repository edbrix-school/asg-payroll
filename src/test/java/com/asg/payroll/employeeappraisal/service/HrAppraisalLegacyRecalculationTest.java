package com.asg.payroll.employeeappraisal.service;

import com.asg.payroll.employeeappraisal.entity.HrAppraisalDtl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HrAppraisalLegacyRecalculationTest {

    @Test
    void monthsBetweenCalendar_matchesLegacyCalendarMonthDiff() {
        LocalDate jan = LocalDate.of(2024, 1, 15);
        LocalDate feb = LocalDate.of(2024, 2, 14);
        assertEquals(1, HrAppraisalLegacyRecalculation.monthsBetweenCalendar(jan, feb));
    }

    @Test
    void applyRecalculateGross_setsNewGrossNetAndIncrementPercent() {
        HrAppraisalDtl row = new HrAppraisalDtl();
        row.setCurBasicSalary(new BigDecimal("1000"));
        row.setCurFaAlw(new BigDecimal("500"));
        row.setCurFixotAlw(BigDecimal.ZERO);
        row.setCurTaAlw(BigDecimal.ZERO);
        row.setCurHraAlw(BigDecimal.ZERO);
        row.setNewBasicSalary(new BigDecimal("1100"));
        row.setNewFaAlw(new BigDecimal("550"));
        row.setNewBonus(new BigDecimal("165"));
        LocalDate periodFrom = LocalDate.now().minusMonths(2).withDayOfMonth(1);
        HrAppraisalLegacyRecalculation.applyRecalculateGross(row, periodFrom, LocalDate.now());
        assertEquals(0, new BigDecimal("1650").compareTo(row.getNewGrossPay()));
        assertEquals(0, new BigDecimal("150").compareTo(row.getNetIncrement()));
        assertEquals("Changed", row.getStatus());
        assertEquals(0, new BigDecimal("10").compareTo(row.getNewIncrementPer()));
        assertEquals(0, new BigDecimal("11").compareTo(row.getNewBonusPer()));
    }

    @Test
    void applyRecalculateGrossFromPercent_forIncrementPercent_updatesAmountsAndGross() {
        HrAppraisalDtl row = new HrAppraisalDtl();
        row.setCurBasicSalary(new BigDecimal("1000"));
        row.setCurFaAlw(new BigDecimal("500"));
        row.setCurFixotAlw(BigDecimal.ZERO);
        row.setCurTaAlw(BigDecimal.ZERO);
        row.setCurHraAlw(BigDecimal.ZERO);
        row.setNewIncrementPer(new BigDecimal("10"));

        HrAppraisalLegacyRecalculation.applyRecalculateGrossFromPercent(row, "NewIncrementPer", LocalDate.now().minusMonths(1), LocalDate.now());

        assertEquals(0, new BigDecimal("1100").compareTo(row.getNewBasicSalary()));
        assertEquals(0, new BigDecimal("550").compareTo(row.getNewFaAlw()));
        assertEquals(0, new BigDecimal("1650").compareTo(row.getNewGrossPay()));
        assertEquals(0, new BigDecimal("150").compareTo(row.getNetIncrement()));
    }

    @Test
    void applyFromNetDifference_splitsNetUsingBasicAndFixedPercent() {
        HrAppraisalDtl row = new HrAppraisalDtl();
        row.setCurBasicSalary(new BigDecimal("1000"));
        row.setCurFaAlw(new BigDecimal("500"));
        row.setCurFixotAlw(BigDecimal.ZERO);
        row.setCurTaAlw(BigDecimal.ZERO);
        row.setCurHraAlw(BigDecimal.ZERO);

        HrAppraisalLegacyRecalculation.applyFromNetDifference(
                row,
                new BigDecimal("100"),
                new BigDecimal("60"),
                new BigDecimal("40"),
                LocalDate.now().minusMonths(1),
                LocalDate.now()
        );

        assertEquals(0, new BigDecimal("1060").compareTo(row.getNewBasicSalary()));
        assertEquals(0, new BigDecimal("540").compareTo(row.getNewFaAlw()));
        assertEquals(0, new BigDecimal("1600").compareTo(row.getNewGrossPay()));
        assertEquals(0, new BigDecimal("100").compareTo(row.getNetIncrement()));
    }
}
