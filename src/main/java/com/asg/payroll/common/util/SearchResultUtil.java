package com.asg.payroll.common.util;

import com.asg.common.lib.dto.RawSearchResult;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;

public class SearchResultUtil {

    private SearchResultUtil() {
    }

    /**
     * Normalizes date values in raw search results so midnight time formats
     * (e.g. 2026-08-05T00:00:00 or 2026-08-05 00:00:00) are returned as YYYY-MM-DD date strings.
     */
    public static RawSearchResult normalizeDates(RawSearchResult raw) {
        if (raw == null || raw.records() == null) {
            return raw;
        }
        for (Map<String, Object> row : raw.records()) {
            if (row == null) continue;
            for (Map.Entry<String, Object> entry : new ArrayList<>(row.entrySet())) {
                Object val = entry.getValue();
                if (val == null) continue;
                if (val instanceof String str) {
                    if (str.matches("^\\d{4}-\\d{2}-\\d{2}[T ]00:00:00(\\.\\d+)?$")) {
                        row.put(entry.getKey(), str.substring(0, 10));
                    }
                } else if (val instanceof java.sql.Date sqlDate) {
                    row.put(entry.getKey(), sqlDate.toLocalDate().toString());
                } else if (val instanceof LocalDate ld) {
                    row.put(entry.getKey(), ld.toString());
                } else if (val instanceof Timestamp ts) {
                    LocalDateTime ldt = ts.toLocalDateTime();
                    if (ldt.toLocalTime().equals(LocalTime.MIDNIGHT)) {
                        row.put(entry.getKey(), ldt.toLocalDate().toString());
                    } else {
                        row.put(entry.getKey(), ldt.toString());
                    }
                }
            }
        }
        return raw;
    }
}
