package com.asg.payroll.payrollvariables.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.payroll.payrollvariables.dto.PayrollVariablesRequestDTO;
import com.asg.payroll.payrollvariables.dto.PayrollVariablesResponseDTO;
import com.asg.payroll.payrollvariables.entity.HrPayrollVariablesHdr;
import com.asg.payroll.payrollvariables.repository.PayrollVariablesRepository;
import com.asg.payroll.payrollvariables.service.PayrollVariablesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayrollVariablesServiceImpl implements PayrollVariablesService {

    private static final String TABLE_NAME = "HR_PAYROLL_VARIABLES_HDR";
    private static final String POID_COLUMN = "TRANSACTION_POID";
    private static final String NOT_FOUND_MSG = "Payroll Variables record not found with ID: ";

    private static final String EMPLOYEE_NAME_LOV = "EMPLOYEE_NAME";

    private final PayrollVariablesRepository repository;
    private final LovDataService lovDataService;
    private final LoggingService loggingService;
    private final DocumentSearchService documentSearchService;
    private final DocumentDeleteService documentDeleteService;

    @Override
    @Transactional
    public PayrollVariablesResponseDTO create(PayrollVariablesRequestDTO request) {
        try {
            HrPayrollVariablesHdr entity = toEntity(request);
            HrPayrollVariablesHdr saved = repository.saveAndFlush(entity);
            HrPayrollVariablesHdr refreshed = findOrThrow(saved.getTransactionPoid());
            loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), refreshed.getTransactionPoid().toString());
            return toDto(refreshed);
        } catch (Exception ex) {
            throw new ValidationException(extractTriggerErrorMessage(ex));
        }
    }

    @Override
    @Transactional
    public PayrollVariablesResponseDTO update(Long transactionPoid, PayrollVariablesRequestDTO request) {
        HrPayrollVariablesHdr existing = findOrThrow(transactionPoid);
        HrPayrollVariablesHdr oldEntity = new HrPayrollVariablesHdr();
        BeanUtils.copyProperties(existing, oldEntity);

        existing.setTransactionDate(request.getTransactionDate());
        existing.setEmployeePoid(request.getEmployeePoid());
        existing.setAmount(request.getAmount());
        existing.setPayrollMonth(request.getPayrollMonth());
        existing.setRemarks(request.getRemarks());

        try {
            HrPayrollVariablesHdr updated = repository.save(existing);
            loggingService.logChanges(oldEntity, updated, HrPayrollVariablesHdr.class,
                    UserContext.getDocumentId(), transactionPoid.toString(), LogDetailsEnum.MODIFIED, POID_COLUMN);
            return toDto(updated);
        } catch (Exception ex) {
            throw new ValidationException(extractTriggerErrorMessage(ex));
        }
    }

    @Override
    public PayrollVariablesResponseDTO getById(Long transactionPoid) {
        return toDto(findOrThrow(transactionPoid));
    }

    @Override
    @Transactional
    public void softDelete(Long transactionPoid, DeleteReasonDto deleteReasonDto) {
        HrPayrollVariablesHdr existing = findOrThrow(transactionPoid);
        documentDeleteService.deleteDocument(transactionPoid, TABLE_NAME, POID_COLUMN, deleteReasonDto, existing.getTransactionDate());
    }

    @Override
    public Map<String, Object> list(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {
        validatePeriodDates(periodFrom, periodTo);
        String operator = documentSearchService.resolveOperator(filters);
        String isDeleted = documentSearchService.resolveIsDeleted(filters);
        List<FilterDto> resolvedFilters = documentSearchService.resolveDateFilters(filters, "TRANSACTION_DATE", periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, resolvedFilters, operator, pageable, isDeleted, "DOC_REF", POID_COLUMN);
        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());
        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    private HrPayrollVariablesHdr findOrThrow(Long transactionPoid) {
        return repository.findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MSG, POID_COLUMN, transactionPoid));
    }

    private HrPayrollVariablesHdr toEntity(PayrollVariablesRequestDTO dto) {
        return HrPayrollVariablesHdr.builder()
                .companyPoid(UserContext.getCompanyPoid())
                .groupPoid(UserContext.getGroupPoid())
                .transactionDate(dto.getTransactionDate())
                .employeePoid(dto.getEmployeePoid())
                .amount(dto.getAmount())
                .payrollMonth(dto.getPayrollMonth())
                .remarks(dto.getRemarks())
                .deleted("N")
                .build();
    }

    private PayrollVariablesResponseDTO toDto(HrPayrollVariablesHdr entity) {
        PayrollVariablesResponseDTO dto = PayrollVariablesResponseDTO.builder()
                .transactionPoid(entity.getTransactionPoid())
                .companyPoid(entity.getCompanyPoid())
                .groupPoid(entity.getGroupPoid())
                .transactionDate(entity.getTransactionDate())
                .docRef(entity.getDocRef())
                .employeePoid(entity.getEmployeePoid())
                .allowanceDeductionPoid(entity.getAllowanceDeductionPoid())
                .amount(entity.getAmount())
                .payrollMonth(entity.getPayrollMonth())
                .remarks(entity.getRemarks())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();

        if (entity.getEmployeePoid() != null) {
            dto.setEmployeeDet(lovDataService.getDetailsByPoidAndLovNameFast(entity.getEmployeePoid(), EMPLOYEE_NAME_LOV));
        }
        return dto;
    }

    private static void validatePeriodDates(LocalDate periodFrom, LocalDate periodTo) {
        if ((periodFrom == null) != (periodTo == null)) {
            throw new IllegalArgumentException("Both periodFrom and periodTo must be specified or both must be empty.");
        }
        if (periodFrom != null && periodFrom.isAfter(periodTo)) {
            throw new IllegalArgumentException("Period From must not be after Period To");
        }
    }

    private String extractTriggerErrorMessage(Exception ex) {
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("ORA-20001")) return "Changes allowed only within current Financial Period";
            if (message.contains("ORA-20002")) return "Transaction date cannot be updated";
        }
        return "Database validation failed: " + (message != null ? message : "Unknown error");
    }
}
