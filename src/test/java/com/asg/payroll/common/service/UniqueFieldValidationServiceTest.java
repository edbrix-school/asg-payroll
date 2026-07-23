package com.asg.payroll.common.service;

import com.asg.payroll.exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UniqueFieldValidationServiceTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @InjectMocks private UniqueFieldValidationService service;

    private void noRows() {
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
    }

    private void oneRow() {
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of(Map.of("1", 1)));
    }

    private String capturedSql() {
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForList(sql.capture(), any(Object[].class));
        return sql.getValue();
    }

    @Test
    void nullValue_ThrowsRequired() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                service.validateRequiredUniqueField("HR_PAYROLL_HDR", "DOC_REF", null, null, null, null));
        assertEquals("Value required for this field", ex.getMessage());
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void emptyValue_ThrowsRequired() {
        assertThrows(ValidationException.class, () ->
                service.validateRequiredUniqueField("HR_PAYROLL_HDR", "DOC_REF", "", null, null, null));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void missingTableName_Throws() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                service.validateRequiredUniqueField("", "DOC_REF", "X", null, null, null));
        assertEquals("TableName not mentioned for Unique Validation", ex.getMessage());
    }

    @Test
    void missingFieldName_Throws() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                service.validateRequiredUniqueField("HR_PAYROLL_HDR", null, "X", null, null, null));
        assertEquals("FieldName not mentioned for Unique Validation", ex.getMessage());
    }

    @Test
    void duplicateFound_ThrowsNotUnique() {
        oneRow();
        ValidationException ex = assertThrows(ValidationException.class, () ->
                service.validateRequiredUniqueField("HR_PAYROLL_HDR", "DOC_REF", "ASG101", null, null, null));
        assertEquals("The value entered is not unique (should not be repeating)...", ex.getMessage());
    }

    @Test
    void noDuplicate_Passes() {
        noRows();
        assertDoesNotThrow(() ->
                service.validateRequiredUniqueField("HR_PAYROLL_HDR", "DOC_REF", "ASG999", null, null, null));
    }

    @Test
    void createMode_OmitsExclusionSoEveryRowIsChecked() {
        noRows();
        service.isDuplicate("HR_PAYROLL_HDR", "DOC_REF", "ASG101", "TRANSACTION_POID", null, null);

        assertFalse(capturedSql().contains("TRANSACTION_POID"));
    }

    @Test
    void editMode_ExcludesCurrentRecord() {
        noRows();
        service.isDuplicate("HR_PAYROLL_HDR", "DOC_REF", "ASG101", "TRANSACTION_POID", 42L, null);

        assertTrue(capturedSql().contains("AND TRANSACTION_POID <> ?"));
    }

    @Test
    void valueIsBoundNotConcatenated() {
        noRows();
        service.isDuplicate("HR_PAYROLL_HDR", "DOC_REF", "O'Brien", null, null, null);

        ArgumentCaptor<Object[]> args = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).queryForList(anyString(), args.capture());
        assertEquals("O'Brien", args.getValue()[0]);
        assertFalse(capturedSql().contains("O'Brien"));
    }

    @Test
    void customValidation_IsApplied() {
        noRows();
        service.isDuplicate("HR_PAYROLL_HDR", "DOC_REF", "ASG101", null, null, "DELETED = 'N'");

        assertTrue(capturedSql().contains("AND (DELETED = 'N')"));
    }

    @Test
    void customValidation_LiteralNullIgnored() {
        noRows();
        service.isDuplicate("HR_PAYROLL_HDR", "DOC_REF", "ASG101", null, null, "null");

        assertFalse(capturedSql().contains("AND ("));
    }

    @Test
    void fieldNameMayBeAnExpression() {
        noRows();
        service.isDuplicate("HR_PAYROLL_HDR", "TO_CHAR(PAYROLL_MONTH,'DD-MON-YYYY')",
                "31-JAN-2024", null, null, null);

        assertTrue(capturedSql().contains("TO_CHAR(PAYROLL_MONTH,'DD-MON-YYYY')"));
    }

    @Test
    void injectedTableName_Rejected() {
        assertThrows(ValidationException.class, () ->
                service.isDuplicate("HR_PAYROLL_HDR; DROP TABLE X", "DOC_REF", "A", null, null, null));
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void injectedCustomValidation_Rejected() {
        assertThrows(ValidationException.class, () ->
                service.isDuplicate("HR_PAYROLL_HDR", "DOC_REF", "A", null, null, "1=1 -- comment"));
        verifyNoInteractions(jdbcTemplate);
    }
}
