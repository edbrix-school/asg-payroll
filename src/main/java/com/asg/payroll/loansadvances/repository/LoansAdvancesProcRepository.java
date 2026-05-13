package com.asg.payroll.loansadvances.repository;

import java.math.BigDecimal;

public interface LoansAdvancesProcRepository {

    String validateRecurring(Long empPoid, BigDecimal monthlyAmt, String recurType);
}
