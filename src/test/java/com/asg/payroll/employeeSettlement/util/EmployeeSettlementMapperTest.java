package com.asg.payroll.employeeSettlement.util;

import com.asg.common.lib.service.LovDataService;
import com.asg.payroll.employeeSettlement.dto.EmployeeSettlementDto;
import com.asg.payroll.employeeSettlement.dto.LoanDeductionDto;
import com.asg.payroll.employeeSettlement.entity.EmployeeSettlementDtl;
import com.asg.payroll.employeeSettlement.entity.LoanDeductionDtl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmployeeSettlementMapperTest {

    @Mock
    private LovDataService lovDataService;

    private EmployeeSettlementDtl buildSampleEntity() {
        EmployeeSettlementDtl entity = new EmployeeSettlementDtl();
        entity.setTransactionPoid(1L);
        entity.setEmployeePoid(100L);
        entity.setCompanyPoid(10L);
        entity.setGroupPoid(1L);
        entity.setSettlementType("FINAL");
        entity.setLeaveType("ANNUAL");
        entity.setBasicSalary(BigDecimal.valueOf(5000));
        entity.setTransactionDate(LocalDate.now());
        entity.setLeaveStartDate(LocalDate.now().minusDays(30));
        entity.setLeaveEndDate(LocalDate.now());
        entity.setDeleted("N");
        entity.setPaymentMethod("BANK");
        entity.setNetSalary(BigDecimal.valueOf(4500));
        entity.setGrossSalary(BigDecimal.valueOf(5000));
        return entity;
    }

    private EmployeeSettlementDto buildSampleDto() {
        return EmployeeSettlementDto.builder()
                .transactionPoid(1L)
                .employeePoid(100L)
                .companyPoid(10L)
                .groupPoid(1L)
                .settlementType("FINAL")
                .leaveType("ANNUAL")
                .basicSalary(BigDecimal.valueOf(5000))
                .transactionDate(LocalDate.now())
                .leaveStartDate(LocalDate.now().minusDays(30))
                .leaveEndDate(LocalDate.now())
                .paymentMethod("BANK")
                .netSalary(BigDecimal.valueOf(4500))
                .grossSalary(BigDecimal.valueOf(5000))
                .build();
    }

    // --- mapToDto ---

    @Test
    void testMapToDto_Success() {
        EmployeeSettlementDtl entity = buildSampleEntity();

        EmployeeSettlementDto dto = EmployeeSettlementMapper.mapToDto(entity, lovDataService);

        assertNotNull(dto);
        assertEquals(1L, dto.getTransactionPoid());
        assertEquals(100L, dto.getEmployeePoid());
        assertEquals("FINAL", dto.getSettlementType());
        assertEquals("ANNUAL", dto.getLeaveType());
        assertEquals(BigDecimal.valueOf(5000), dto.getBasicSalary());
        assertEquals("BANK", dto.getPaymentMethod());
    }

    @Test
    void testMapToDto_NullEntity_ReturnsNull() {
        EmployeeSettlementDto dto = EmployeeSettlementMapper.mapToDto(null, lovDataService);
        assertNull(dto);
    }

    @Test
    void testMapToDto_AllFieldsMapped() {
        EmployeeSettlementDtl entity = buildSampleEntity();
        entity.setDocRef("DOC-001");
        entity.setRemarks("Test remarks");
        entity.setTotalSettlementAmt(BigDecimal.valueOf(10000));
        entity.setDateOfLeaving(LocalDate.now());

        EmployeeSettlementDto dto = EmployeeSettlementMapper.mapToDto(entity, lovDataService);

        assertEquals("DOC-001", dto.getDocRef());
        assertEquals("Test remarks", dto.getRemarks());
        assertEquals(BigDecimal.valueOf(10000), dto.getTotalSettlementAmt());
        assertEquals(entity.getDateOfLeaving(), dto.getDateOfLeaving());
    }

    // --- mapCreateDtoToEntity ---

    @Test
    void testMapCreateDtoToEntity_Success() {
        EmployeeSettlementDto dto = buildSampleDto();
        EmployeeSettlementDtl entity = new EmployeeSettlementDtl();

        EmployeeSettlementMapper.mapCreateDtoToEntity(dto, entity);

        assertEquals(100L, entity.getEmployeePoid());
        assertEquals(10L, entity.getCompanyPoid());
        assertEquals("FINAL", entity.getSettlementType());
        assertEquals("ANNUAL", entity.getLeaveType());
        assertEquals(BigDecimal.valueOf(5000), entity.getBasicSalary());
        assertEquals("N", entity.getDeleted()); // always set to "N" on create
    }

    @Test
    void testMapCreateDtoToEntity_SetsDeletedToN() {
        EmployeeSettlementDto dto = buildSampleDto();
        EmployeeSettlementDtl entity = new EmployeeSettlementDtl();

        EmployeeSettlementMapper.mapCreateDtoToEntity(dto, entity);

        assertEquals("N", entity.getDeleted());
    }

    @Test
    void testMapCreateDtoToEntity_NullableFieldsHandled() {
        EmployeeSettlementDto dto = EmployeeSettlementDto.builder()
                .employeePoid(200L)
                .companyPoid(20L)
                .build();
        EmployeeSettlementDtl entity = new EmployeeSettlementDtl();

        EmployeeSettlementMapper.mapCreateDtoToEntity(dto, entity);

        assertEquals(200L, entity.getEmployeePoid());
        assertNull(entity.getSettlementType());
        assertNull(entity.getBasicSalary());
    }

    // --- mapUpdateDtoToEntity ---

    @Test
    void testMapUpdateDtoToEntity_OnlyNonNullFieldsUpdated() {
        EmployeeSettlementDtl entity = buildSampleEntity();
        String originalSettlementType = entity.getSettlementType();

        EmployeeSettlementDto dto = EmployeeSettlementDto.builder()
                .basicSalary(BigDecimal.valueOf(6000))
                // settlementType intentionally null — should not overwrite
                .build();

        EmployeeSettlementMapper.mapUpdateDtoToEntity(dto, entity);

        assertEquals(BigDecimal.valueOf(6000), entity.getBasicSalary());
        assertEquals(originalSettlementType, entity.getSettlementType()); // unchanged
    }

    @Test
    void testMapUpdateDtoToEntity_UpdatesAllProvidedFields() {
        EmployeeSettlementDtl entity = buildSampleEntity();
        EmployeeSettlementDto dto = buildSampleDto();
        dto.setSettlementType("LEAVE");
        dto.setRemarks("Updated remarks");
        dto.setNetSalary(BigDecimal.valueOf(4800));

        EmployeeSettlementMapper.mapUpdateDtoToEntity(dto, entity);

        assertEquals("LEAVE", entity.getSettlementType());
        assertEquals("Updated remarks", entity.getRemarks());
        assertEquals(BigDecimal.valueOf(4800), entity.getNetSalary());
    }

    // --- mapLoanDtlListToDto ---

    @Test
    void testMapLoanDtlListToDto_Success() {
        LoanDeductionDtl loan = new LoanDeductionDtl();
        loan.setDetRowId(1L);
        loan.setTransactionPoid(1L);
        loan.setEmployeePoid(100L);
        loan.setRecurAmount(BigDecimal.valueOf(300));
        loan.setRecurType("LOAN");

        List<LoanDeductionDto> result = EmployeeSettlementMapper.mapLoanDtlListToDto(List.of(loan));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(300), result.get(0).getRecurAmount());
        assertEquals("LOAN", result.get(0).getRecurType());
    }

    @Test
    void testMapLoanDtlListToDto_EmptyList() {
        List<LoanDeductionDto> result = EmployeeSettlementMapper.mapLoanDtlListToDto(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testMapLoanDtlListToDto_NullList_ReturnsNull() {
        List<LoanDeductionDto> result = EmployeeSettlementMapper.mapLoanDtlListToDto(null);
        assertNull(result);
    }

    // --- mapLoanDtlListFromDto ---

    @Test
    void testMapLoanDtlListFromDto_Success() {
        LoanDeductionDto dto = LoanDeductionDto.builder()
                .employeePoid(100L)
                .recurAmount(BigDecimal.valueOf(500))
                .recurType("LOAN")
                .refNo("REF-001")
                .build();

        List<LoanDeductionDtl> result = EmployeeSettlementMapper.mapLoanDtlListFromDto(List.of(dto), 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getTransactionPoid());
        assertEquals(BigDecimal.valueOf(500), result.get(0).getRecurAmount());
    }

    @Test
    void testMapLoanDtlListFromDto_EmptyList() {
        List<LoanDeductionDtl> result = EmployeeSettlementMapper.mapLoanDtlListFromDto(List.of(), 1L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
