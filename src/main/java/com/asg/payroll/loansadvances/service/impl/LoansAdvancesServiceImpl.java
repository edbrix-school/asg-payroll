package com.asg.payroll.loansadvances.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.payroll.exceptions.ValidationException;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductRequest;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductResponse;
import com.asg.payroll.loansadvances.entity.HrRecurringPayDeduct;
import com.asg.payroll.loansadvances.repository.HrRecurringPayDeductRepository;
import com.asg.payroll.loansadvances.repository.LoansAdvancesProcRepository;
import com.asg.payroll.loansadvances.service.LoansAdvancesService;
import com.asg.payroll.loansadvances.util.LoansAdvancesMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoansAdvancesServiceImpl implements LoansAdvancesService {

    private final HrRecurringPayDeductRepository repository;
    private final LoansAdvancesProcRepository loansAdvancesProcRepository;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;

    private static final String TRANSACTION_POID = "TRANSACTION_POID";
    private static final String TRANSACTIONPOID = "transaction poid";
    private static final String LOANSADVANCES = "Loans/Advances";

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional
    public Long create(HrRecurringPayDeductRequest request) {

        validate(request);

        Long groupPoid = UserContext.getGroupPoid();
        Long companyPoid = UserContext.getCompanyPoid();
        HrRecurringPayDeduct entity = new HrRecurringPayDeduct();
        LoansAdvancesMapper.mapToEntity(request, entity);
        entity.setGroupPoid(groupPoid);
        entity.setCompanyPoid(companyPoid.toString());
        entity.setDeleted("N");

        HrRecurringPayDeduct hdr = repository.saveAndFlush(entity);
        entityManager.refresh(hdr);

        log.info("Created Loan/Advance with ID: {}", entity.getTransactionPoid());

        // Log the creation
        String key = entity.getTransactionPoid().toString();
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(),hdr.getTransactionPoid().toString(), String.format("%s %s", LogDetailsEnum.CREATED.getDescription(), hdr.getDocRef()));

        return entity.getTransactionPoid();
    }

    @Override
    public HrRecurringPayDeductResponse getById(Long id) {

        HrRecurringPayDeduct entity = repository.findByTransactionPoid(id)
                .orElseThrow(() -> new ResourceNotFoundException(LOANSADVANCES, TRANSACTIONPOID, id));

        log.info("Fetched Loan/Advance with ID: {}", entity.getTransactionPoid());

        return LoansAdvancesMapper.mapToResponse(entity);
    }

    @Override
    @Transactional
    public HrRecurringPayDeductResponse update(Long id, HrRecurringPayDeductRequest request) {

        HrRecurringPayDeduct entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(LOANSADVANCES, TRANSACTIONPOID, id));

        validate(request);

        // Create a copy of the existing entity for logging
        HrRecurringPayDeduct oldEntity = new HrRecurringPayDeduct();
        BeanUtils.copyProperties(entity, oldEntity);

        LoansAdvancesMapper.mapToEntity(request, entity);

        HrRecurringPayDeduct updated = repository.save(entity);
        log.info("Updated Loan/Advance with ID: {}", updated.getTransactionPoid());

        // Log the update with changes
        String key = updated.getTransactionPoid().toString();
        loggingService.logChanges(oldEntity, updated, HrRecurringPayDeduct.class, UserContext.getDocumentId(), key,
                LogDetailsEnum.MODIFIED, TRANSACTION_POID);

        return LoansAdvancesMapper.mapToResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, DeleteReasonDto deleteReasonDto) {

        repository.findByTransactionPoid(id)
                .orElseThrow(() -> new ResourceNotFoundException(LOANSADVANCES, TRANSACTIONPOID, id));

        // Use DocumentDeleteService for deletion (handles logging internally)
        documentDeleteService.deleteDocument(id, "HR_RECURRING_PAY_DEDUCT", TRANSACTION_POID, deleteReasonDto, null);

        log.info("Soft deleted Loan/Advance with ID: {}", id);
    }

    @Override
    public Map<String, Object> list(FilterRequestDto filterRequest, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {
        validatePeriodDates(periodFrom, periodTo);
        String operator = documentSearchService.resolveOperator(filterRequest);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequest);
        List<FilterDto> filterList = documentSearchService.resolveDateFilters(filterRequest, "TRANSACTION_DATE", periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(UserContext.getDocumentId(), filterList, operator, pageable,
                isDeleted, "DESCRIPTION", TRANSACTION_POID);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());
        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    private static void validatePeriodDates(LocalDate periodFrom, LocalDate periodTo) {
        if ((periodFrom == null) != (periodTo == null)) {
            throw new ValidationException("Both periodFrom and periodTo must be specified or both must be empty.");
        }
        if (periodFrom != null && periodFrom.isAfter(periodTo)) {
            throw new ValidationException("Period From must not be after Period To");
        }
    }

    // =========================  VALIDATION (From SRS)  =========================
    private void validate(HrRecurringPayDeductRequest request) {

        // Settled validation
        if ("Y".equalsIgnoreCase(request.getSettledAndClosed()) && (request.getReceiptNo() == null || request.getReceiptDate() == null))
            throw new ValidationException("Receipt No and Date required when settled");

        // Business rule
        if (request.getTotalAmount() != null && request.getMonthlyAmount() != null
                && request.getMonthlyAmount().compareTo(request.getTotalAmount()) > 0)
            throw new ValidationException("Monthly Amount should not be greater than Total Amount");

        String result = loansAdvancesProcRepository.validateRecurring(
                request.getEmployeePoid(),
                request.getMonthlyAmount(),
                request.getRecurType()
        );

        if (result == null || !result.toLowerCase().contains("true")) {
            throw new ValidationException("Validation failed: " + result);
        }

    }

}
