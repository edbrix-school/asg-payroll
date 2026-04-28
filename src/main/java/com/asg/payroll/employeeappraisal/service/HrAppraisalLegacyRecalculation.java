package com.asg.payroll.employeeappraisal.service;

import com.asg.payroll.employeeappraisal.entity.HrAppraisalDtl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Ports {@code HRAppraisalBean#RecalculateGross} from legacy ADF (asg_payroll) so persisted
 * detail rows match the same derived fields as the interactive screen: auto-copy of select
 * "new" allowances from current when null, gross / net increment / increment %, bonus %, arrears.
 */
public final class HrAppraisalLegacyRecalculation {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private HrAppraisalLegacyRecalculation() {
    }

    /**
     * Calendar month delta between two dates (year/month only), matching
     * {@code HRAppraisalBean#monthsBetween(java.util.Date, java.util.Date)}.
     */
    public static int monthsBetweenCalendar(LocalDate startInclusive, LocalDate endInclusive) {
        if (startInclusive == null || endInclusive == null) {
            return 0;
        }
        int yearDiff = endInclusive.getYear() - startInclusive.getYear();
        return yearDiff * 12 + endInclusive.getMonthValue() - startInclusive.getMonthValue();
    }

    public static void applyRecalculateGross(HrAppraisalDtl row, LocalDate periodFromHeader, LocalDate today) {
        if (row == null) {
            return;
        }
        copyNullNewAllowancesFromCurrent(row);

        BigDecimal basicSal = nz(row.getCurBasicSalary());
        BigDecimal fixAllowance = nz(row.getCurFaAlw());
        BigDecimal fixOt = nz(row.getCurFixotAlw());
        BigDecimal travelAllowance = nz(row.getCurTaAlw());
        BigDecimal hra = nz(row.getCurHraAlw());
        BigDecimal gross = basicSal.add(fixAllowance).add(fixOt).add(travelAllowance).add(hra);

        BigDecimal newBasicSal = nz(row.getNewBasicSalary());
        BigDecimal newFixAllowance = nz(row.getNewFaAlw());
        BigDecimal newFixOt = nz(row.getNewFixotAlw());
        BigDecimal newTravelAllowance = nz(row.getNewTaAlw());
        BigDecimal newHra = nz(row.getNewHraAlw());
        BigDecimal newGross = newBasicSal.add(newFixAllowance).add(newFixOt).add(newTravelAllowance).add(newHra);
        row.setNewGrossPay(newGross);

        BigDecimal netDiff = newGross.subtract(gross);
        row.setNetIncrement(netDiff);
        if (netDiff.compareTo(ZERO) != 0) {
            row.setStatus("Changed");
        }

        if (gross.compareTo(ZERO) != 0) {
            BigDecimal netDiffPercent = netDiff.divide(gross, 2, RoundingMode.HALF_UP)
                    .multiply(HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);
            row.setNewIncrementPer(netDiffPercent);
        }

        BigDecimal bonus = nz(row.getNewBonus());
        BigDecimal curFixedGross = basicSal.add(fixAllowance);
        if (bonus.compareTo(ZERO) != 0 && curFixedGross.compareTo(ZERO) != 0) {
            BigDecimal bonusPercent = bonus.divide(curFixedGross, 2, RoundingMode.HALF_UP)
                    .multiply(HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);
            row.setNewBonusPer(bonusPercent);
        }

        if (periodFromHeader != null && today != null && periodFromHeader.isBefore(today)) {
                int months = monthsBetweenCalendar(periodFromHeader, today);
                if (months > 0) {
                    BigDecimal arrears = netDiff.multiply(BigDecimal.valueOf(months));
                    row.setArrears(arrears);
                }
            }

    }

    public static void applyRecalculateGrossFromPercent(HrAppraisalDtl row, String fieldName, LocalDate periodFromHeader, LocalDate today) {
        if (row == null) {
            return;
        }
        copyNullNewAllowancesFromCurrent(row);

        String normalizedField = fieldName == null ? "" : fieldName.trim();
        BigDecimal basicSal = nz(row.getCurBasicSalary());
        BigDecimal fixAllowance = nz(row.getCurFaAlw());

        if ("NewIncrementPer".equalsIgnoreCase(normalizedField)) {
            BigDecimal netDiffPercent = nz(row.getNewIncrementPer());
            BigDecimal newBasicSal = basicSal.add(basicSal.multiply(netDiffPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP));
            BigDecimal newFixAllowance = fixAllowance.add(fixAllowance.multiply(netDiffPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP));
            row.setNewBasicSalary(newBasicSal);
            row.setNewFaAlw(newFixAllowance);
            applyRecalculateGross(row, periodFromHeader, today);
            return;
        }

        if ("NewBonusPer".equalsIgnoreCase(normalizedField)) {
            BigDecimal bonusPercent = nz(row.getNewBonusPer());
            BigDecimal bonus = basicSal.add(fixAllowance).multiply(bonusPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            row.setNewBonus(bonus);
        }
    }

    public static void applyFromNetDifference(HrAppraisalDtl row,
                                              BigDecimal netDiffAmount,
                                              BigDecimal appraisalBasicPercent,
                                              BigDecimal appraisalFixedPercent,
                                              LocalDate periodFromHeader,
                                              LocalDate today) {
        if (row == null) {
            return;
        }
        BigDecimal netDiff = nz(netDiffAmount);
        BigDecimal basicPercent = nz(appraisalBasicPercent);
        nz(appraisalFixedPercent);

        BigDecimal curBasicSalary = nz(row.getCurBasicSalary());
        BigDecimal curFaAlw = nz(row.getCurFaAlw());
        BigDecimal newBasicSalary = curBasicSalary;
        BigDecimal newFaAlw = curFaAlw;

        if (netDiff.compareTo(ZERO) > 0) {
            if (curFaAlw.compareTo(ZERO) > 0) {
                BigDecimal basicDiff = netDiff.multiply(basicPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
                BigDecimal fixedDiff = netDiff.subtract(basicDiff);
                newBasicSalary = curBasicSalary.add(basicDiff);
                newFaAlw = curFaAlw.add(fixedDiff);
            } else {
                newBasicSalary = curBasicSalary.add(netDiff);
            }
        }

        row.setNewBasicSalary(newBasicSalary);
        row.setNewFaAlw(newFaAlw);
        applyRecalculateGross(row, periodFromHeader, today);
    }

    private static void copyNullNewAllowancesFromCurrent(HrAppraisalDtl row) {
        if (row.getCurFixotAlw() != null && row.getNewFixotAlw() == null) {
            row.setNewFixotAlw(row.getCurFixotAlw());
        }
        if (row.getCurHraAlw() != null && row.getNewHraAlw() == null) {
            row.setNewHraAlw(row.getCurHraAlw());
        }
        if (row.getCurTaAlw() != null && row.getNewTaAlw() == null) {
            row.setNewTaAlw(row.getCurTaAlw());
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : ZERO;
    }
}
