package com.asg.payroll.salarydetails.util;

import com.asg.payroll.salarydetails.dto.SalaryAllowanceDto;
import com.asg.payroll.salarydetails.dto.SalaryDetailRequest;
import com.asg.payroll.salarydetails.dto.SalaryDetailResponse;
import com.asg.payroll.salarydetails.dto.SalaryHistoryDto;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryAlwDtl;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryHist;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryMaster;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalaryDetailsMapperTest {

    @Test
    void testMapToEntity_Success() {
        SalaryDetailRequest request = SalaryDetailRequest.builder()
                .employeePoid(100L)
                .basicSalary(1000L)
                .loanDeductionAmt(50L)
                .paymentMethod("BANK")
                .ibanAccountNo("IBAN123")
                .lastIncrementDate(LocalDate.now())
                .build();

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        SalaryDetailsMapper.mapToEntity(request, entity);

        assertEquals(100L, entity.getEmployeePoid());
        assertEquals(1000L, entity.getBasicSalary());
        assertEquals(50L, entity.getLoanDeductionAmt());
        assertEquals("BANK", entity.getPaymentMethod());
        assertEquals("IBAN123", entity.getIbanAccountNo());
        assertEquals(request.getLastIncrementDate(), entity.getLastIncrementDate());
    }

    @Test
    void testMapToEntity_NullRequest() {
        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        entity.setEmployeePoid(1L);
        SalaryDetailsMapper.mapToEntity(null, entity);
        assertEquals(1L, entity.getEmployeePoid());
    }

    @Test
    void testMapToEntity_LoanDeductionNull() {
        SalaryDetailRequest request = new SalaryDetailRequest();
        request.setLoanDeductionAmt(null);

        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        SalaryDetailsMapper.mapToEntity(request, entity);

        assertEquals(0L, entity.getLoanDeductionAmt());
    }

    @Test
    void testMapToResponse_Success() {
        HrEmployeeSalaryMaster entity = HrEmployeeSalaryMaster.builder()
                .salaryPoid(1L)
                .employeePoid(100L)
                .basicSalary(1000L)
                .build();

        HrEmployeeSalaryAlwDtl alw = HrEmployeeSalaryAlwDtl.builder()
                .amount(BigDecimal.valueOf(100))
                .build();
        List<HrEmployeeSalaryAlwDtl> allowances = List.of(alw);

        HrEmployeeSalaryHist hist = HrEmployeeSalaryHist.builder()
                .basicSalary(900L)
                .build();
        List<HrEmployeeSalaryHist> history = List.of(hist);

        SalaryDetailResponse response = SalaryDetailsMapper.mapToResponse(entity, allowances, history);

        assertNotNull(response);
        assertEquals(1L, response.getSalaryPoid());
        assertEquals(100L, response.getEmployeePoid());
        assertEquals(1, response.getAllowances().size());
        assertEquals(1, response.getHistory().size());
    }

    @Test
    void testMapToResponse_NullEntity() {
        assertNull(SalaryDetailsMapper.mapToResponse(null, null, null));
    }

    @Test
    void testMapToResponse_NullLists() {
        HrEmployeeSalaryMaster entity = new HrEmployeeSalaryMaster();
        SalaryDetailResponse response = SalaryDetailsMapper.mapToResponse(entity, null, null);
        assertNotNull(response);
        assertNull(response.getAllowances());
        assertNull(response.getHistory());
    }

    @Test
    void testMapToAllowanceDto() {
        HrEmployeeSalaryAlwDtl entity = HrEmployeeSalaryAlwDtl.builder()
                .allowanceDeductionPoid(10L)
                .amount(BigDecimal.valueOf(100))
                .build();

        SalaryAllowanceDto dto = SalaryDetailsMapper.mapToAllowanceDto(entity);
        assertNotNull(dto);
        assertEquals(10L, dto.getAllowanceDeductionPoid());
        assertEquals(BigDecimal.valueOf(100), dto.getAmount());
    }

    @Test
    void testMapToAllowanceDto_Null() {
        assertNull(SalaryDetailsMapper.mapToAllowanceDto(null));
    }

    @Test
    void testMapToHistoryDto() {
        HrEmployeeSalaryHist entity = HrEmployeeSalaryHist.builder()
                .detRowId(1L)
                .designationPoid(10L)
                .lastIncrementDate(LocalDate.now())
                .build();

        SalaryHistoryDto dto = SalaryDetailsMapper.mapToHistoryDto(entity);
        assertNotNull(dto);
        assertEquals(1L, dto.getDetRowId());
        assertEquals("10", dto.getDesignation());
    }

    @Test
    void testMapToHistoryDto_Null() {
        assertNull(SalaryDetailsMapper.mapToHistoryDto(null));
    }

    @Test
    void testMapToHistoryDto_DetRowIdNull() {
        HrEmployeeSalaryHist entity = new HrEmployeeSalaryHist();
        entity.setDetRowId(null);
        SalaryHistoryDto dto = SalaryDetailsMapper.mapToHistoryDto(entity);
        assertNotNull(dto);
        assertNull(dto.getDetRowId());
    }

    @Test
    void testMapDtoToAllowanceEntity() {
        SalaryAllowanceDto dto = SalaryAllowanceDto.builder()
                .allowanceDeductionPoid(10L)
                .amount(BigDecimal.valueOf(100))
                .detRowId(5L)
                .build();
        HrEmployeeSalaryAlwDtl entity = new HrEmployeeSalaryAlwDtl();

        SalaryDetailsMapper.mapDtoToAllowanceEntity(dto, entity, 1L);

        assertEquals(10L, entity.getAllowanceDeductionPoid());
        assertEquals(BigDecimal.valueOf(100), entity.getAmount());
        assertEquals(1L, entity.getSalaryPoid());
        assertEquals(5L, entity.getDetRowId());
    }

    @Test
    void testMapDtoToAllowanceEntity_Nulls() {
        assertNull(SalaryDetailsMapper.mapDtoToAllowanceEntity(null, new HrEmployeeSalaryAlwDtl(), 1L));
        assertNull(SalaryDetailsMapper.mapDtoToAllowanceEntity(new SalaryAllowanceDto(), null, 1L));
    }
}