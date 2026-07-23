package com.asg.payroll.common.service;

import com.asg.payroll.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Port of the legacy ADF {@code ValidatorForRequiredUniqueField} /
 * {@code FUNC_GLOBAL_UNIQUE_CHECKING} pair: a value must be present, and must not already
 * exist in the given table, ignoring the record currently being edited.
 * <p>
 * Semantics kept from the Oracle function:
 * <ul>
 *   <li>Every row is scanned - there is no DELETED filter and no company/group scoping.
 *       The function accepts login group/company/user but never references them.</li>
 *   <li>Values are compared case-insensitively after stripping spaces, dots, hyphens and
 *       ampersands, so "AB-01" and "ab 01" collide.</li>
 *   <li>The exclusion clause is only applied when a poid value is supplied, so create mode
 *       checks every row.</li>
 * </ul>
 * Two deliberate departures, both noted on the relevant members: values are bound as JDBC
 * parameters rather than concatenated, and {@code customValidation} actually takes effect.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UniqueFieldValidationService {

    /** Legacy ValidatorForRequiredUniqueField wording; callers reporting their own outcome reuse these. */
    public static final String VALUE_REQUIRED = "Value required for this field";
    public static final String NOT_UNIQUE = "The value entered is not unique (should not be repeating)...";

    private static final String TABLE_REQUIRED = "TableName not mentioned for Unique Validation";
    private static final String FIELD_REQUIRED = "FieldName not mentioned for Unique Validation";

    /** Plain identifiers: table names and poid column names. */
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_$#]*");
    /** Statement terminators and comment markers, rejected in developer-supplied SQL fragments. */
    private static final Pattern SQL_BREAKERS = Pattern.compile(";|--|/\\*|\\*/");

    /** Strips the characters the legacy function ignores when comparing two values. */
    private static final String NORMALISE =
            "UPPER(LTRIM(RTRIM(REPLACE(REPLACE(REPLACE(REPLACE(%s,' ',''),'.',''),'-',''),'&',''))))";

    private final JdbcTemplate jdbcTemplate;

    /**
     * Equivalent of {@code ValidatorForRequiredUniqueField}: throws when the value is blank
     * or already used.
     *
     * @param tableName        table to search, e.g. HR_PAYROLL_HDR
     * @param fieldName        column or SQL expression, e.g. TO_CHAR(PAYROLL_MONTH,'DD-MON-YYYY').
     *                         Developer-supplied SQL - never pass user input here.
     * @param fieldValue       value being validated
     * @param poidFieldName    primary key column used to exclude the current record, may be null
     * @param poidFieldValue   primary key of the record being edited; null in create mode, which
     *                         drops the exclusion so every row is checked
     * @param customValidation extra predicate ANDed onto the lookup, may be null.
     *                         Developer-supplied SQL - never pass user input here.
     */
    public void validateRequiredUniqueField(String tableName, String fieldName, Object fieldValue,
                                            String poidFieldName, Object poidFieldValue,
                                            String customValidation) {
        if (fieldValue == null || fieldValue.toString().isEmpty()) {
            throw new ValidationException(VALUE_REQUIRED);
        }
        if (isDuplicate(tableName, fieldName, fieldValue, poidFieldName, poidFieldValue, customValidation)) {
            throw new ValidationException(NOT_UNIQUE);
        }
    }

    /**
     * Equivalent of {@code FUNC_GLOBAL_UNIQUE_CHECKING}: true when another row already holds
     * this value. Unlike the Oracle function this returns a boolean rather than the strings
     * "TRUE"/"FALSE"/"FALSE2", and it lets SQL errors propagate instead of swallowing them
     * into a "FALSE " || SQLERRM result that reads as "unique".
     */
    public boolean isDuplicate(String tableName, String fieldName, Object fieldValue,
                               String poidFieldName, Object poidFieldValue, String customValidation) {
        if (tableName == null || tableName.isEmpty()) {
            throw new ValidationException(TABLE_REQUIRED);
        }
        if (fieldName == null || fieldName.isEmpty()) {
            throw new ValidationException(FIELD_REQUIRED);
        }
        requireIdentifier(tableName, "TableName");
        requireSafeFragment(fieldName, "FieldName");

        StringBuilder sql = new StringBuilder("SELECT 1 FROM ").append(tableName)
                .append(" WHERE ").append(String.format(NORMALISE, fieldName))
                .append(" = ").append(String.format(NORMALISE, "?"));
        List<Object> args = new ArrayList<>();
        args.add(fieldValue.toString());

        // Legacy applies the exclusion only when a poid is supplied, so create mode sees every row.
        if (poidFieldName != null && !poidFieldName.isEmpty() && poidFieldValue != null) {
            requireIdentifier(poidFieldName, "PoidFieldName");
            sql.append(" AND ").append(poidFieldName).append(" <> ?");
            args.add(poidFieldValue);
        }
        // FUNC_GLOBAL_UNIQUE_CHECKING wraps its whole body in "IF P_CUSTOM_STRING IS NULL",
        // so a non-null custom string makes it skip the lookup and report the value as unique.
        // That is plainly not the intent, so here the fragment is applied as an extra predicate.
        if (customValidation != null && !customValidation.isBlank()
                && !"NULL".equalsIgnoreCase(customValidation.trim())) {
            requireSafeFragment(customValidation, "CustomValidation");
            sql.append(" AND (").append(customValidation).append(")");
        }
        sql.append(" FETCH FIRST 1 ROWS ONLY");

        log.debug("Unique check: {} args={}", sql, args);
        return !jdbcTemplate.queryForList(sql.toString(), args.toArray()).isEmpty();
    }

    private void requireIdentifier(String value, String label) {
        if (!IDENTIFIER.matcher(value).matches()) {
            throw new ValidationException(label + " is not a valid identifier: " + value);
        }
    }

    private void requireSafeFragment(String value, String label) {
        if (SQL_BREAKERS.matcher(value).find()) {
            throw new ValidationException(label + " contains illegal SQL: " + value);
        }
    }
}
