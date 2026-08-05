package com.asg.payroll.common.util;

import com.asg.common.lib.dto.RawSearchResult;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchResultUtilTest {

    @Test
    void testNormalizeDates_MidnightTimeFormattedAsDateOnly() {
        Map<String, Object> row = new HashMap<>();
        row.put("TRANSACTION_DATE", "2026-08-05T00:00:00");
        row.put("DELETED_DATE", "2026-08-05 00:00:00");
        row.put("SQL_DATE", java.sql.Date.valueOf("2026-08-05"));
        row.put("LOCAL_DATE", LocalDate.of(2026, 8, 5));
        row.put("MIDNIGHT_TS", Timestamp.valueOf("2026-08-05 00:00:00"));
        row.put("DATE_WITH_TIME", "2026-08-05T14:30:00");

        RawSearchResult raw = new RawSearchResult(List.of(row), Map.of(), 1L);
        SearchResultUtil.normalizeDates(raw);

        assertEquals("2026-08-05", row.get("TRANSACTION_DATE"));
        assertEquals("2026-08-05", row.get("DELETED_DATE"));
        assertEquals("2026-08-05", row.get("SQL_DATE"));
        assertEquals("2026-08-05", row.get("LOCAL_DATE"));
        assertEquals("2026-08-05", row.get("MIDNIGHT_TS"));
        assertEquals("2026-08-05T14:30:00", row.get("DATE_WITH_TIME"));
    }
}
