package com.asg.payroll.payrollprocess.repository;

import com.asg.payroll.payrollprocess.entity.HrPayrollHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HrPayrollHdrRepository extends JpaRepository<HrPayrollHdr, Long> {

    /**
     * Returns DOC_REFs of non-deleted payrolls already booked against the given payroll
     * month, ignoring the record being edited. Mirrors the legacy
     * ValidatorForRequiredUniqueField check on HR_PAYROLL_HDR.PAYROLL_MONTH.
     *
     * @param excludePoid TRANSACTION_POID to skip; pass a non-existent id (e.g. -1) for create mode
     */
    @Query("""
            SELECT h.docRef FROM HrPayrollHdr h
            WHERE h.payrollMonth = :payrollMonth
              AND (h.deleted = 'N' OR h.deleted IS NULL)
              AND h.transactionPoid <> :excludePoid
            ORDER BY h.transactionPoid
            """)
    List<String> findDocRefsByPayrollMonthExcluding(@Param("payrollMonth") LocalDate payrollMonth,
                                                    @Param("excludePoid") Long excludePoid);
}
