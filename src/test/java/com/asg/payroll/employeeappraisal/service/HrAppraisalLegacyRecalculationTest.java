package com.asg.payroll.employeeappraisal.service;

import com.asg.payroll.employeeappraisal.entity.HrAppraisalDtl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class HrAppraisalLegacyRecalculationTest {

    private HrAppraisalDtl dtlWithBasicAndFixed(double basic, double fixed) {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurBasicSalary(BigDecimal.valueOf(basic));
        dtl.setCurFaAlw(BigDecimal.valueOf(fixed));
        dtl.setNewBasicSalary(BigDecimal.valueOf(basic + 500));
        dtl.setNewFaAlw(BigDecimal.valueOf(fixed + 200));
        return dtl;
    }

    // --- monthsBetweenCalendar ---

    @Test
    void monthsBetweenCalendar_SameMonth_ReturnsZero() {
        assertEquals(0, HrAppraisalLegacyRecalculation.monthsBetweenCalendar(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)));
    }

    @Test
    void monthsBetweenCalendar_ThreeMonthsApart_ReturnsThree() {
        assertEquals(3, HrAppraisalLegacyRecalculation.monthsBetweenCalendar(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 1)));
    }

    @Test
    void monthsBetweenCalendar_AcrossYear_ReturnsCorrect() {
        assertEquals(14, HrAppraisalLegacyRecalculation.monthsBetweenCalendar(
                LocalDate.of(2023, 1, 1), LocalDate.of(2024, 3, 1)));
    }

    @Test
    void monthsBetweenCalendar_NullStart_ReturnsZero() {
        assertEquals(0, HrAppraisalLegacyRecalculation.monthsBetweenCalendar(null, LocalDate.now()));
    }

    @Test
    void monthsBetweenCalendar_NullEnd_ReturnsZero() {
        assertEquals(0, HrAppraisalLegacyRecalculation.monthsBetweenCalendar(LocalDate.now(), null));
    }

    // --- copyNullNewAllowancesFromCurrent: already-set branches ---

    @Test
    void copyNullNewAllowances_AlreadySet_NotOverwritten() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurFixotAlw(BigDecimal.valueOf(100));
        dtl.setNewFixotAlw(BigDecimal.valueOf(50)); // already set — must not be overwritten
        dtl.setCurHraAlw(BigDecimal.valueOf(200));  // newHraAlw null — should be copied
        dtl.setCurTaAlw(BigDecimal.valueOf(300));   // newTaAlw null — should be copied

        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.now(), LocalDate.now());

        assertEquals(BigDecimal.valueOf(50), dtl.getNewFixotAlw());
        assertEquals(BigDecimal.valueOf(200), dtl.getNewHraAlw());
        assertEquals(BigDecimal.valueOf(300), dtl.getNewTaAlw());
    }

    @Test
    void copyNullNewAllowances_NullCurrent_NotCopied() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurFixotAlw(BigDecimal.valueOf(100)); // cur set, new null — should be copied
        // curHraAlw and curTaAlw are null — nothing to copy
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.now(), LocalDate.now());
        assertEquals(BigDecimal.valueOf(100), dtl.getNewFixotAlw());
        assertNull(dtl.getNewHraAlw());
        assertNull(dtl.getNewTaAlw());
    }

    @Test
    void applyRecalculateGross_ZeroGross_NoIncrementPercent() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        // all salaries zero → gross == 0, skip increment percent branch
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.now(), LocalDate.now());
        assertNull(dtl.getNewIncrementPer());
    }

    @Test
    void applyRecalculateGross_ZeroBonus_NoBonusPercent() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);
        dtl.setNewBonus(BigDecimal.ZERO);
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.now(), LocalDate.now());
        assertNull(dtl.getNewBonusPer());
    }

    @Test
    void applyRecalculateGross_NullBonus_TreatedAsZero_NoBonusPercent() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);
        // newBonus left null — nz() returns ZERO, bonus branch skipped
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.now(), LocalDate.now());
        assertNull(dtl.getNewBonusPer());
    }

    @Test
    void applyRecalculateGross_ZeroCurFixedGross_NoBonusPercent() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setNewBonus(BigDecimal.valueOf(100));
        // curBasicSalary and curFaAlw are null → curFixedGross == 0
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.now(), LocalDate.now());
        assertNull(dtl.getNewBonusPer());
    }

    @Test
    void applyRecalculateGross_PeriodFromAfterToday_NoArrears() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);
        LocalDate future = LocalDate.now().plusMonths(3);
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, future, LocalDate.now());
        assertNull(dtl.getArrears());
    }

    @Test
    void applyRecalculateGross_ZeroMonths_NoArrears() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);
        // periodFrom == today → monthsBetween == 0
        LocalDate same = LocalDate.of(2024, 3, 15);
        LocalDate today = LocalDate.of(2024, 3, 1); // before same, so isBefore is false
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, same, today);
        assertNull(dtl.getArrears());
    }

    @Test
    void applyFromNetDifference_NegativeNetDiff_NoChange() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurBasicSalary(BigDecimal.valueOf(5000));
        dtl.setCurFaAlw(BigDecimal.valueOf(2000));

        HrAppraisalLegacyRecalculation.applyFromNetDifference(
                dtl, BigDecimal.valueOf(-100),
                BigDecimal.valueOf(60), BigDecimal.valueOf(40),
                LocalDate.now(), LocalDate.now());

        // netDiff <= 0 branch: newBasic and newFaAlw stay as current
        assertEquals(0, BigDecimal.valueOf(5000).compareTo(dtl.getNewBasicSalary()));
        assertEquals(0, BigDecimal.valueOf(2000).compareTo(dtl.getNewFaAlw()));
    }

    @Test
    void applyRecalculateGross_NullRow_DoesNothing() {
        assertDoesNotThrow(() -> HrAppraisalLegacyRecalculation.applyRecalculateGross(null, LocalDate.now(), LocalDate.now()));
    }

    @Test
    void applyRecalculateGross_ComputesNewGrossAndNetIncrement() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);

        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1));

        // newGross = 5500 + 2200 = 7700
        assertEquals(0, new BigDecimal("7700").compareTo(dtl.getNewGrossPay()));
        // netIncrement = 7700 - 7000 = 700
        assertEquals(0, new BigDecimal("700").compareTo(dtl.getNetIncrement()));
    }

    @Test
    void applyRecalculateGross_CopyNullNewAllowances_CurNull_NotCopied() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        // curFixotAlw/curHraAlw/curTaAlw are null
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.now(), LocalDate.now());
        assertNull(dtl.getNewFixotAlw());
        assertNull(dtl.getNewHraAlw());
        assertNull(dtl.getNewTaAlw());
    }

    @Test
    void applyRecalculateGross_CopyNullNewAllowances_NewAlreadySet_NotOverwritten() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurFixotAlw(BigDecimal.valueOf(100));
        dtl.setNewFixotAlw(BigDecimal.valueOf(50));
        dtl.setCurHraAlw(BigDecimal.valueOf(200));
        dtl.setNewHraAlw(BigDecimal.valueOf(80));
        dtl.setCurTaAlw(BigDecimal.valueOf(300));
        dtl.setNewTaAlw(BigDecimal.valueOf(120));

        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.now(), LocalDate.now());

        assertEquals(BigDecimal.valueOf(50), dtl.getNewFixotAlw());
        assertEquals(BigDecimal.valueOf(80), dtl.getNewHraAlw());
        assertEquals(BigDecimal.valueOf(120), dtl.getNewTaAlw());
    }

    @Test
    void applyRecalculateGross_SetsStatusChangedWhenNetDiffNonZero() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);

        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1));

        assertEquals("Changed", dtl.getStatus());
    }

    @Test
    void applyRecalculateGross_ComputesIncrementPercent() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);

        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1));

        // netDiff=700, gross=7000 => 700/7000*100 = 10.00
        assertEquals(new BigDecimal("10.00"), dtl.getNewIncrementPer());
    }

    @Test
    void applyRecalculateGross_ComputesArrears_WhenPeriodFromBeforeToday() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);
        LocalDate periodFrom = LocalDate.of(2024, 1, 1);
        LocalDate today = LocalDate.of(2024, 4, 1); // 3 months later

        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, periodFrom, today);

        // arrears = 700 * 3 = 2100
        assertEquals(0, new BigDecimal("2100").compareTo(dtl.getArrears()));
    }

    @Test
    void applyRecalculateGross_NullPeriodFrom_NoArrears() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, null, LocalDate.now());
        assertNull(dtl.getArrears());
    }

    @Test
    void applyRecalculateGross_NullToday_NoArrears() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.of(2024, 1, 1), null);
        assertNull(dtl.getArrears());
    }

    @Test
    void applyRecalculateGross_PeriodFromBeforeToday_ZeroMonths_NoArrears() {
        // periodFrom is before today but same month → monthsBetween == 0
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);
        LocalDate periodFrom = LocalDate.of(2024, 3, 1);
        LocalDate today = LocalDate.of(2024, 3, 31); // same month, isBefore=true but months=0
        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, periodFrom, today);
        assertNull(dtl.getArrears());
    }

    @Test
    void applyRecalculateGross_CopiesNullNewAllowancesFromCurrent() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurFixotAlw(BigDecimal.valueOf(300));
        dtl.setCurHraAlw(BigDecimal.valueOf(400));
        dtl.setCurTaAlw(BigDecimal.valueOf(200));
        // new allowances are null — should be copied from current

        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.now(), LocalDate.now());

        assertEquals(BigDecimal.valueOf(300), dtl.getNewFixotAlw());
        assertEquals(BigDecimal.valueOf(400), dtl.getNewHraAlw());
        assertEquals(BigDecimal.valueOf(200), dtl.getNewTaAlw());
    }

    @Test
    void applyRecalculateGross_ComputesBonusPercent_WhenBonusAndGrossNonZero() {
        HrAppraisalDtl dtl = dtlWithBasicAndFixed(5000, 2000);
        dtl.setNewBonus(BigDecimal.valueOf(700));

        HrAppraisalLegacyRecalculation.applyRecalculateGross(dtl, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1));

        // bonus=700, curFixedGross=5000+2000=7000 => 700/7000*100 = 10.00
        assertEquals(new BigDecimal("10.00"), dtl.getNewBonusPer());
    }

    // --- applyRecalculateGrossFromPercent ---

    @Test
    void applyRecalculateGrossFromPercent_NullRow_DoesNothing() {
        assertDoesNotThrow(() -> HrAppraisalLegacyRecalculation.applyRecalculateGrossFromPercent(
                null, "NewIncrementPer", LocalDate.now(), LocalDate.now()));
    }

    @Test
    void applyRecalculateGrossFromPercent_NewIncrementPer_RecalculatesBasicAndFixed() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurBasicSalary(BigDecimal.valueOf(5000));
        dtl.setCurFaAlw(BigDecimal.valueOf(2000));
        dtl.setNewIncrementPer(BigDecimal.valueOf(10));

        HrAppraisalLegacyRecalculation.applyRecalculateGrossFromPercent(
                dtl, "NewIncrementPer", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1));

        // newBasic = 5000 + 5000*10/100 = 5500
        assertEquals(new BigDecimal("5500.00"), dtl.getNewBasicSalary());
        // newFixed = 2000 + 2000*10/100 = 2200
        assertEquals(new BigDecimal("2200.00"), dtl.getNewFaAlw());
    }

    @Test
    void applyRecalculateGrossFromPercent_NewBonusPer_RecalculatesBonus() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurBasicSalary(BigDecimal.valueOf(5000));
        dtl.setCurFaAlw(BigDecimal.valueOf(2000));
        dtl.setNewBonusPer(BigDecimal.valueOf(10));

        HrAppraisalLegacyRecalculation.applyRecalculateGrossFromPercent(
                dtl, "NewBonusPer", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1));

        // bonus = (5000+2000) * 10/100 = 700
        assertEquals(new BigDecimal("700.00"), dtl.getNewBonus());
    }

    @Test
    void applyRecalculateGrossFromPercent_NullFieldName_NoChange() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurBasicSalary(BigDecimal.valueOf(5000));
        // fieldName null → normalizedField = "" → no branch matches
        assertDoesNotThrow(() -> HrAppraisalLegacyRecalculation.applyRecalculateGrossFromPercent(
                dtl, null, LocalDate.now(), LocalDate.now()));
    }

    @Test
    void applyRecalculateGrossFromPercent_UnknownField_NoChange() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurBasicSalary(BigDecimal.valueOf(5000));

        assertDoesNotThrow(() -> HrAppraisalLegacyRecalculation.applyRecalculateGrossFromPercent(
                dtl, "UnknownField", LocalDate.now(), LocalDate.now()));
    }

    // --- applyFromNetDifference ---

    @Test
    void applyFromNetDifference_NullRow_DoesNothing() {
        assertDoesNotThrow(() -> HrAppraisalLegacyRecalculation.applyFromNetDifference(
                null, BigDecimal.valueOf(500), BigDecimal.valueOf(60), BigDecimal.valueOf(40),
                LocalDate.now(), LocalDate.now()));
    }

    @Test
    void applyFromNetDifference_WithFixedAllowance_SplitsBasicAndFixed() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurBasicSalary(BigDecimal.valueOf(5000));
        dtl.setCurFaAlw(BigDecimal.valueOf(2000));

        HrAppraisalLegacyRecalculation.applyFromNetDifference(
                dtl, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(60), BigDecimal.valueOf(40),
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1));

        // basicDiff = 1000 * 60/100 = 600 => newBasic = 5600
        assertEquals(new BigDecimal("5600.00"), dtl.getNewBasicSalary());
        // fixedDiff = 1000 - 600 = 400 => newFixed = 2400
        assertEquals(new BigDecimal("2400.00"), dtl.getNewFaAlw());
    }

    @Test
    void applyFromNetDifference_NoFixedAllowance_AllGoesToBasic() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurBasicSalary(BigDecimal.valueOf(5000));
        dtl.setCurFaAlw(BigDecimal.ZERO);

        HrAppraisalLegacyRecalculation.applyFromNetDifference(
                dtl, BigDecimal.valueOf(1000),
                BigDecimal.valueOf(60), BigDecimal.valueOf(40),
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1));

        // all goes to basic => newBasic = 6000
        assertEquals(new BigDecimal("6000"), dtl.getNewBasicSalary());
        assertEquals(BigDecimal.ZERO, dtl.getNewFaAlw());
    }

    @Test
    void applyFromNetDifference_ZeroNetDiff_NoChange() {
        HrAppraisalDtl dtl = new HrAppraisalDtl();
        dtl.setCurBasicSalary(BigDecimal.valueOf(5000));
        dtl.setCurFaAlw(BigDecimal.valueOf(2000));

        HrAppraisalLegacyRecalculation.applyFromNetDifference(
                dtl, BigDecimal.ZERO,
                BigDecimal.valueOf(60), BigDecimal.valueOf(40),
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1));

        assertEquals(new BigDecimal("5000"), dtl.getNewBasicSalary());
        assertEquals(new BigDecimal("2000"), dtl.getNewFaAlw());
    }
}
