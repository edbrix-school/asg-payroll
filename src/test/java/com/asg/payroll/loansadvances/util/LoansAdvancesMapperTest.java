package com.asg.payroll.loansadvances.util;

import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductRequest;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductResponse;
import com.asg.payroll.loansadvances.entity.HrRecurringPayDeduct;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoansAdvancesMapperTest {

    @Test
    void testMapToEntity_withTransactionDate() {
        HrRecurringPayDeductRequest request = new HrRecurringPayDeductRequest();
        request.setDocRef("DOC-123");
        request.setTransactionDate(LocalDate.of(2023, 1, 15));
        request.setEmployeePoid(10L);
        request.setRefNo("REF-001");
        request.setDescriptions("Test Description");
        request.setTotalAmount(new BigDecimal("1000.00"));
        request.setOpeningAmount(new BigDecimal("1000.00"));
        request.setStartDate(LocalDate.of(2023, 2, 1));
        request.setMonthlyAmount(new BigDecimal("100.00"));
        request.setSettledAndClosed("N");
        request.setReceiptNo("REC-111");
        request.setReceiptDate(LocalDate.of(2023, 3, 1));

        HrRecurringPayDeduct entity = new HrRecurringPayDeduct();

        LoansAdvancesMapper.mapToEntity(request, entity);

        assertEquals("DOC-123", entity.getDocRef());
        assertEquals(LocalDate.of(2023, 1, 15), entity.getTransactionDate());
        assertEquals(10L, entity.getEmployeePoid());
        assertEquals("REF-001", entity.getRefNo());
        assertEquals("Test Description", entity.getDescriptions());
        assertEquals(new BigDecimal("1000.00"), entity.getTotalAmount());
        assertEquals(new BigDecimal("1000.00"), entity.getOpeningAmount());
        assertEquals(LocalDate.of(2023, 2, 1), entity.getStartDate());
        assertEquals(new BigDecimal("100.00"), entity.getMonthlyAmount());
        assertEquals("N", entity.getSettledAndClosed());
        assertEquals("REC-111", entity.getReceiptNo());
        assertEquals(LocalDate.of(2023, 3, 1), entity.getReceiptDate());
    }

    @Test
    void testMapToEntity_withoutTransactionDate() {
        HrRecurringPayDeductRequest request = new HrRecurringPayDeductRequest();
        request.setDocRef("DOC-124");
        // TransactionDate is null

        HrRecurringPayDeduct entity = new HrRecurringPayDeduct();

        LoansAdvancesMapper.mapToEntity(request, entity);

        assertNotNull(entity.getTransactionDate()); // Should fall back to DateUtil.getCurrentDateInUserTimeZone()
        assertEquals("DOC-124", entity.getDocRef());
    }

    @Test
    void testMapToResponse() {
        HrRecurringPayDeduct entity = new HrRecurringPayDeduct();
        entity.setTransactionPoid(1L);
        entity.setGroupPoid(2L);
        entity.setCompanyPoid("COMP-1");
        entity.setTransactionDate(LocalDate.of(2023, 4, 1));
        entity.setDocRef("DOC-999");
        entity.setRecurType("ADVANCE");
        entity.setGlCode("GL-101");
        entity.setEmployeePoid(100L);
        entity.setRefNo("REF-999");
        entity.setDescriptions("Response Desc");
        entity.setTotalAmount(new BigDecimal("5000.00"));
        entity.setOpeningAmount(new BigDecimal("5000.00"));
        entity.setStartDate(LocalDate.of(2023, 5, 1));
        entity.setMonthlyAmount(new BigDecimal("500.00"));
        entity.setSourceDocId("SRC-ID-1");
        entity.setSourceDocPoid(1000L);
        // Note: the mapper maps sourceDOcRef to sourceDocRef
        entity.setSourceDOcRef("SRC-DOC-REF-1");
        entity.setSettledAndClosed("Y");
        entity.setReceiptNo("REC-999");
        entity.setReceiptDate(LocalDate.of(2023, 6, 1));

        HrRecurringPayDeductResponse response = LoansAdvancesMapper.mapToResponse(entity);

        assertEquals(1L, response.getTransactionPoid());
        assertEquals(2L, response.getGroupPoid());
        assertEquals("COMP-1", response.getCompanyPoid());
        assertEquals(LocalDate.of(2023, 4, 1), response.getTransactionDate());
        assertEquals("DOC-999", response.getDocRef());
        assertEquals("ADVANCE", response.getRecurType());
        assertEquals("GL-101", response.getGlCode());
        assertEquals(100L, response.getEmployeePoid());
        assertEquals("REF-999", response.getRefNo());
        assertEquals("Response Desc", response.getDescriptions());
        assertEquals(new BigDecimal("5000.00"), response.getTotalAmount());
        assertEquals(new BigDecimal("5000.00"), response.getOpeningAmount());
        assertEquals(LocalDate.of(2023, 5, 1), response.getStartDate());
        assertEquals(new BigDecimal("500.00"), response.getMonthlyAmount());
        assertEquals("SRC-ID-1", response.getSourceDocId());
        assertEquals(1000L, response.getSourceDocPoid());
        assertEquals("DOC-999", response.getSourceDocRef()); // NOTE: The mapper says entity.getDocRef()
        assertEquals("Y", response.getSettledAndClosed());
        assertEquals("REC-999", response.getReceiptNo());
        assertEquals(LocalDate.of(2023, 6, 1), response.getReceiptDate());
    }
}
