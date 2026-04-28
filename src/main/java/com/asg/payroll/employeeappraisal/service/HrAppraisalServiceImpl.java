package com.asg.payroll.employeeappraisal.service;

import com.asg.common.lib.dto.*;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.PrintService;
import com.asg.common.lib.utility.PaginationUtil;
import org.springframework.context.annotation.Lazy;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

import javax.sql.DataSource;

import com.asg.payroll.employeeappraisal.dto.HrAppraisalActionRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalDtlRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRecalculationRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRequest;
import com.asg.payroll.employeeappraisal.entity.HrAppraisalDtl;
import com.asg.payroll.employeeappraisal.entity.HrAppraisalDtlId;
import com.asg.payroll.employeeappraisal.entity.HrAppraisalHdr;
import com.asg.payroll.employeeappraisal.enums.ActionType;
import com.asg.payroll.employeeappraisal.repository.HrAppraisalDtlRepository;
import com.asg.payroll.employeeappraisal.repository.HrAppraisalHdrRepository;
import com.asg.payroll.exceptions.ResourceNotFoundException;
import com.asg.payroll.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@Transactional
public class HrAppraisalServiceImpl implements HrAppraisalService {

    public HrAppraisalServiceImpl(@Lazy HrAppraisalService self, HrAppraisalHdrRepository hdrRepository, HrAppraisalDtlRepository dtlRepository, DocumentSearchService documentSearchService, DocumentDeleteService documentDeleteService, LoggingService loggingService, PrintService printService, DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.self = self;
        this.hdrRepository = hdrRepository;
        this.dtlRepository = dtlRepository;
        this.documentSearchService = documentSearchService;
        this.documentDeleteService = documentDeleteService;
        this.loggingService = loggingService;
        this.printService = printService;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }
    private final HrAppraisalService self;
    private final HrAppraisalHdrRepository hdrRepository;
    private final HrAppraisalDtlRepository dtlRepository;
    private final DocumentSearchService documentSearchService;
    private final DocumentDeleteService documentDeleteService;
    private final LoggingService loggingService;
    private final PrintService printService;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;


    private static final String P_TRANS_POID = "P_TRANS_POID";
    private static final String P_LOGIN_COMPANY_POID = "P_LOGIN_COMPANY_POID";
    private static final String P_LOGIN_USER = "P_LOGIN_USER";
    private static final String P_BASIC_INCREMENT_PERCENT = "P_BASIC_INCREMENT_PERCENT";
    private static final String P_LOGIN_USER_POID = "P_LOGIN_USER_POID";
    private static final String P_TRANSACTION_POID = "P_TRANSACTION_POID";
    private static final String P_COMPANYID = "P_COMPANYID";
    private static final String P_BONUS_PERCENT = "P_BONUS_PERCENT";
    private static final String TRANSACTION_POID = "TRANSACTION_POID";
    private static final String APPRAISAL_NOT_FOUND = "Employee appraisal not found with ID: ";
    private static final String P_STATUS = "P_STATUS";

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listAppraisals(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {
        String operator = documentSearchService.resolveOperator(filters);
        String isDeleted = documentSearchService.resolveIsDeleted(filters);
        List<FilterDto> resolvedFilters = documentSearchService.resolveDateFilters(filters, "TRANSACTION_DATE", periodFrom, periodTo);
        RawSearchResult raw = documentSearchService.search(documentId, resolvedFilters, operator, pageable, isDeleted, "DOC_REF", TRANSACTION_POID);
        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());
        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAppraisalById(Long transactionPoid) {
        HrAppraisalHdr hdr = hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(APPRAISAL_NOT_FOUND + transactionPoid));
        List<HrAppraisalDtl> details = dtlRepository.findByTransactionPoid(transactionPoid);
        Map<String, Object> out = new HashMap<>();
        out.put("header", hdr);
        out.put("details", details);
        return out;
    }

    @Override
    public Map<String, Object> createAppraisal(HrAppraisalRequest request) {
        validateHeaderForLegacySave(request);
        HrAppraisalHdr hdr = new HrAppraisalHdr();
        mapHeader(request, hdr);
        hdr.setGroupPoid(UserContext.getGroupPoid());
        hdr.setCompanyPoid(UserContext.getCompanyPoid());
        hdr.setDeleted("N");
        HrAppraisalHdr saved = hdrRepository.saveAndFlush(hdr);
        upsertDetails(saved.getTransactionPoid(), request.getDetails(), true);
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), saved.getTransactionPoid().toString(),String.format("%s %s", LogDetailsEnum.CREATED, saved.getDocRef()));
        return self.getAppraisalById(saved.getTransactionPoid());
    }

    @Override
    public Map<String, Object> updateAppraisal(Long transactionPoid, HrAppraisalRequest request) {
        validateHeaderForLegacySave(request);
        HrAppraisalHdr existing = hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(APPRAISAL_NOT_FOUND + transactionPoid));
        HrAppraisalHdr oldEntity = new HrAppraisalHdr();
        BeanUtils.copyProperties(existing, oldEntity);
        mapHeader(request, existing);
        hdrRepository.save(existing);
        upsertDetails(transactionPoid, request.getDetails(), false);
        loggingService.logChanges(oldEntity,existing,HrAppraisalHdr.class,UserContext.getDocumentId(),transactionPoid.toString(),LogDetailsEnum.MODIFIED,TRANSACTION_POID);
        return self.getAppraisalById(transactionPoid);
    }

    @Override
    public void deleteAppraisal(Long transactionPoid, DeleteReasonDto deleteReasonDto) {
        HrAppraisalHdr hdr = hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(APPRAISAL_NOT_FOUND + transactionPoid));

        documentDeleteService.deleteDocument(
                transactionPoid,
                "HR_APPRAISAL_HDR",
                TRANSACTION_POID,
                deleteReasonDto,
                hdr.getTransactionDate()
        );
    }

    @Override
    public Map<String, Object> getDetailsSp(Long transactionPoid, Long employeePoid) {
        return execute(
                "PROC_HR_APPRAISAL_GET_DETAILS",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlParameter("P_EMP_POID", Types.NUMERIC), new SqlOutParameter("OUTDATA", OracleTypes.CURSOR), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid, "P_EMP_POID", employeePoid)
        );
    }

    @Override
    public Map<String, Object> loadAppraisalDataSp(Long transactionPoid, String actionType) {
        if (actionType == null || actionType.isBlank()) {
            throw new ValidationException("Appraisal load action type is required (legacy: LOAD_EMPLOYEES_BLANK_DATA or LOAD_EMPLOYEES_WITH_CURRENT_DATA).");
        }
        return execute(
                "PROC_HR_LOAD_APPRAISAL_DATA",
                List.of(new SqlParameter(P_TRANS_POID, Types.NUMERIC), new SqlParameter(P_LOGIN_USER, Types.VARCHAR), new SqlParameter(P_LOGIN_COMPANY_POID, Types.NUMERIC), new SqlParameter("P_ACTION_TYPE", Types.VARCHAR), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_TRANS_POID, transactionPoid, P_LOGIN_USER, UserContext.getUserId(), P_LOGIN_COMPANY_POID, UserContext.getCompanyPoid(), "P_ACTION_TYPE", actionType)
        );
    }

    @Override
    public Map<String, Object> clearAppraisalDataSp(Long transactionPoid) {
        return execute(
                "PROC_HR_CLEAR_APPRAISAL_DATA",
                List.of(new SqlParameter(P_TRANS_POID, Types.NUMERIC), new SqlParameter(P_LOGIN_USER, Types.VARCHAR), new SqlParameter(P_LOGIN_COMPANY_POID, Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_TRANS_POID, transactionPoid, P_LOGIN_USER, UserContext.getUserId(), P_LOGIN_COMPANY_POID, UserContext.getCompanyPoid())
        );
    }

    @Override
    public Map<String, Object> batchUpdateSp(Long transactionPoid, HrAppraisalActionRequest request) {
        BigDecimal basic = nz(request.getBasicIncrementPercent());
        BigDecimal bonus = nz(request.getBonusPercent());
        if (basic.compareTo(BigDecimal.ZERO) == 0 && bonus.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException("Both Basic and Bonus Percentages are zero...");
        }
        return execute(
                "PROC_HR_APPRAISAL_BATCH_UPDATE",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlParameter(P_BASIC_INCREMENT_PERCENT, Types.NUMERIC), new SqlParameter(P_BONUS_PERCENT, Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid, P_BASIC_INCREMENT_PERCENT, request.getBasicIncrementPercent(), P_BONUS_PERCENT, request.getBonusPercent())
        );
    }

    @Override
    public Map<String, Object> recalculateDetail(HrAppraisalRecalculationRequest request) {
        if (request == null || request.getDetail() == null) {
            throw new ValidationException("Recalculation payload and detail are required.");
        }
        if (request.getMode() == null || request.getMode().isBlank()) {
            throw new ValidationException("Recalculation mode is required.");
        }

        HrAppraisalDtl row = new HrAppraisalDtl();
        mapDetail(request.getDetail(), row);
        LocalDate today = LocalDate.now();
        String mode = request.getMode().trim().toUpperCase(Locale.ROOT);

        switch (mode) {
            case "AMOUNT_EDIT" ->
                    HrAppraisalLegacyRecalculation.applyRecalculateGross(row, request.getPeriodFrom(), today);
            case "PERCENT_EDIT" ->
                    HrAppraisalLegacyRecalculation.applyRecalculateGrossFromPercent(row, request.getFieldName(), request.getPeriodFrom(), today);
            case "NET_DIFF_EDIT" -> {
                if (request.getAppraisalBasicPercent() == null) {
                    throw new ValidationException("Appraisal Basic Percent should not be empty for autocalculation of basic and fixed allowances..");
                }
                if (request.getAppraisalFixedPercent() == null) {
                    throw new ValidationException("Appraisal Fixed Percent should not be empty for autocalculation of basic and fixed allowances..");
                }
                BigDecimal basicPercent = nz(request.getAppraisalBasicPercent());
                BigDecimal fixedPercent = nz(request.getAppraisalFixedPercent());
                if (basicPercent.add(fixedPercent).compareTo(new BigDecimal("100")) != 0) {
                    throw new ValidationException("Some of Basic and Fixed percentages should be 100 for autocalculation of basic and fixed allowances..");
                }
                HrAppraisalLegacyRecalculation.applyFromNetDifference(row, nz(row.getNetIncrement()), basicPercent, fixedPercent, request.getPeriodFrom(), today);
            }
            default ->
                    throw new ValidationException("Unsupported recalculation mode. Use AMOUNT_EDIT, PERCENT_EDIT, or NET_DIFF_EDIT.");
        }

        Map<String, Object> out = new HashMap<>();
        out.put("detail", row);
        out.put("mode", mode);
        return out;
    }

    @Override
    public Map<String, Object> updateDataSp(Long transactionPoid, Long employeePoid, HrAppraisalActionRequest request) {
        return execute(
                "PROC_HR_APPRAISAL_UPDATE_DATA",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC), new SqlParameter("P_EMPLOYEE_POID", Types.NUMERIC), new SqlParameter("P_ADDITIONAL_DATA", Types.VARCHAR), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid, P_LOGIN_USER_POID, UserContext.getUserPoid(), "P_EMPLOYEE_POID", employeePoid, "P_ADDITIONAL_DATA", request.getAdditionalData())
        );
    }

    @Override
    public Map<String, Object> updateMasterSp(Long transactionPoid, HrAppraisalActionRequest request) {
        HrAppraisalHdr hdr = hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(APPRAISAL_NOT_FOUND + transactionPoid));
        if (hdr.getApprovedBy() == null || hdr.getApprovedBy().isBlank()) {
            throw new ValidationException("Please enter approval by details... ");
        }
        return execute(
                "PROC_HR_APPRAISAL_UPDATE_MAST",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC), new SqlParameter(P_BASIC_INCREMENT_PERCENT, Types.NUMERIC), new SqlParameter(P_BONUS_PERCENT, Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid, P_LOGIN_USER_POID, UserContext.getUserPoid(), P_BASIC_INCREMENT_PERCENT, request.getBasicIncrementPercent(), P_BONUS_PERCENT, request.getBonusPercent())
        );
    }

    @Override
    public Map<String, Object> createJvSp(Long transactionPoid) {
        return execute(
                "PROC_HR_APPRAISAL_CREATE_JV",
                List.of(new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC), new SqlParameter("P_APPRAISAL_POID", Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_LOGIN_USER_POID, UserContext.getUserPoid(), "P_APPRAISAL_POID", transactionPoid)
        );
    }

    @Override
    public Map<String, Object> sendEmailSp(Long transactionPoid, String resend) {
        return execute(
                "PROC_APPRAISAL_SEND_EMAIL",
                List.of(new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlParameter("P_RESEND", Types.VARCHAR), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_LOGIN_USER_POID, UserContext.getUserPoid(), P_TRANSACTION_POID, transactionPoid, "P_RESEND", resend)
        );
    }

    @Override
    public Map<String, Object> bankFileSp(Long transactionPoid) {
        return execute(
                "PROC_HR_APPRAISAL_BANK_FILE",
                List.of(new SqlParameter("P_COMPANY_POID", Types.NUMERIC), new SqlParameter("P_TRNNO", Types.NUMERIC), new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC), new SqlOutParameter("P_FILE_NAME", Types.VARCHAR)),
                params("P_COMPANY_POID", UserContext.getCompanyPoid(), "P_TRNNO", transactionPoid, P_LOGIN_USER_POID, UserContext.getUserPoid())
        );
    }

    @Override
    public Map<String, Object> arrearsSp(Long transactionPoid, Long payrollPoid) {
        if (payrollPoid == null) {
            throw new ValidationException("Please select the payroll for adding the arrears... ");
        }
        return execute(
                "PROC_HR_APPRAISAL_ARREARS",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC), new SqlParameter("P_PAYROLL_POID", Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid, P_LOGIN_USER_POID, UserContext.getUserPoid(), "P_PAYROLL_POID", payrollPoid)
        );
    }

    @Override
    public Map<String, Object> arrearsCalcSp(Long transactionPoid) {
        return execute(
                "PROC_HR_APPRAISAL_ARREARS_CALC",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid)
        );
    }

    @Override
    public byte[] printA3(Long transactionPoid) throws JRException {
        return generatePdf("EmpAppraisal_A3.jrxml", transactionPoid);
    }

    @Override
    public byte[] printByCompany(Long transactionPoid) throws JRException {
        return generatePdf("EmpAppraisal_ByCompany.jrxml", transactionPoid);
    }

    @Override
    public byte[] printBank(Long transactionPoid) throws JRException {
        return generatePdf("HrAppraisalBankReportPrint.jrxml", transactionPoid);
    }

    @Override
    public byte[] printLetter(Long transactionPoid, Long employeePoid) throws JRException {
        return generatePdf("HrAppraisalLetter.jrxml", transactionPoid, employeePoid);
    }

    private byte[] generatePdf(String reportFile, Long transactionPoid) throws JRException {
        return generatePdf(reportFile, transactionPoid, null);
    }

    private byte[] generatePdf(String reportFile, Long transactionPoid, Long employeePoid) throws JRException {
        Map<String, Object> params = printService.buildBaseParams(transactionPoid, UserContext.getDocumentId());
        params.put(TRANSACTION_POID, transactionPoid);
        if (employeePoid != null) {
            params.put("EMPLOYEE_POID", employeePoid);
        }
        JasperReport report = printService.load(reportFile);
        try {
            return printService.fillReportToPdf(report, params, dataSource);
        } catch (JRException e) {
            throw e;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    private Map<String, Object> execute(String procedureName, List<org.springframework.jdbc.core.SqlParameter> parameters, Map<String, Object> inParams) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName(procedureName)
                .declareParameters(parameters.toArray(new org.springframework.jdbc.core.SqlParameter[0]));
        return call.execute(inParams);
    }

    private void mapHeader(HrAppraisalRequest request, HrAppraisalHdr entity) {
        entity.setTransactionDate(request.getTransactionDate());
        entity.setPeriodFrom(request.getPeriodFrom());
        entity.setPeriodTo(request.getPeriodTo());
        entity.setDescription(request.getDescription());
        entity.setVerifiedBy(request.getVerifiedBy());
        entity.setApprovedBy(request.getApprovedBy());
        entity.setCompleted(request.getCompleted());
        entity.setCompletedOn(request.getCompletedOn());
        entity.setGridListingMethod(request.getGridListingMethod());
        entity.setTotalArrears(request.getTotalArrears());
        entity.setJvDocRef(request.getJvDocRef());
        entity.setJvDocPoid(request.getJvDocPoid());
        entity.setArrearsPayrollPoid(request.getArrearsPayrollPoid());
        entity.setLetterEmailedOn(request.getLetterEmailedOn());
        entity.setAppraisalBasicPercent(request.getAppraisalBasicPercent());
        entity.setAppraisalFixedPercent(request.getAppraisalFixedPercent());
    }

    private ActionType resolveAction(HrAppraisalDtlRequest request, boolean createMode) {
        ActionType action = createMode && request.getActionType() == null ? ActionType.isCreated : request.getActionType();
        return (action == null || action == ActionType.noChange) ? null : action;
    }

    private void upsertDetails(Long transactionPoid, List<HrAppraisalDtlRequest> requests, boolean createMode) {
        if (requests == null || requests.isEmpty()) return;
        HrAppraisalHdr hdr = hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(APPRAISAL_NOT_FOUND + transactionPoid));
        LocalDate today = LocalDate.now();
        for (HrAppraisalDtlRequest request : requests) {
            ActionType action = resolveAction(request, createMode);
            if (action == null) continue;
            if (action == ActionType.isCreated) {
                Long nextDetRowId = dtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                HrAppraisalDtl entity = new HrAppraisalDtl();
                entity.setTransactionPoid(transactionPoid);
                entity.setDetRowId(nextDetRowId);
                entity.setDeleted("N");
                mapDetail(request, entity);
                HrAppraisalLegacyRecalculation.applyRecalculateGross(entity, hdr.getPeriodFrom(), today);
                HrAppraisalDtl saved = dtlRepository.save(entity);
                String logDetail = String.format("Row Created on [Employee Appraisal Details] with DetRowId: %s", saved.getDetRowId());
                loggingService.createLogSummaryEntry(UserContext.getDocumentId(), String.valueOf(transactionPoid), logDetail);
            } else {
                HrAppraisalDtlId id = new HrAppraisalDtlId();
                id.setTransactionPoid(transactionPoid);
                id.setDetRowId(request.getDetRowId());
                if (action == ActionType.isDeleted) {
                    HrAppraisalDtl detail = dtlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Details", "DET_ROW_ID", id.getDetRowId()));
                    dtlRepository.deleteById(id);
                    loggingService.logDelete(detail, UserContext.getDocumentId(), id.toString());
                } else {
                    HrAppraisalDtl existing = dtlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appraisal detail not found for detRowId: " + request.getDetRowId()));
                    HrAppraisalDtl oldEntity = new HrAppraisalDtl();
                    BeanUtils.copyProperties(existing, oldEntity);
                    mapDetail(request, existing);
                    HrAppraisalLegacyRecalculation.applyRecalculateGross(existing, hdr.getPeriodFrom(), today);
                    HrAppraisalDtl saved = dtlRepository.save(existing);
                    String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", saved.getTransactionPoid(), saved.getDetRowId());
                    loggingService.createLog(oldEntity, existing, HrAppraisalDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                }
            }
        }
    }

    private void mapDetail(HrAppraisalDtlRequest request, HrAppraisalDtl entity) {
        entity.setEmployeePoid(request.getEmployeePoid());
        entity.setDesignationPoid(request.getDesignationPoid());
        entity.setJoinDate(request.getJoinDate());
        entity.setCurAirEntitle(request.getCurAirEntitle());
        entity.setCurBonus(request.getCurBonus());
        entity.setCurIncrement(request.getCurIncrement());
        entity.setCurBasicSalary(request.getCurBasicSalary());
        entity.setCurFaAlw(request.getCurFaAlw());
        entity.setCurTaAlw(request.getCurTaAlw());
        entity.setCurHraAlw(request.getCurHraAlw());
        entity.setCurFixotAlw(request.getCurFixotAlw());
        entity.setCurSplAlw(request.getCurSplAlw());
        entity.setCurOthAlw(request.getCurOthAlw());
        entity.setCurAvgot(request.getCurAvgot());
        entity.setCurGrossPay(request.getCurGrossPay());
        entity.setNewAirEntitle(request.getNewAirEntitle());
        entity.setNewBonus(request.getNewBonus());
        entity.setNewIncrementPer(request.getNewIncrementPer());
        entity.setNewBasicSalary(request.getNewBasicSalary());
        entity.setNewFaAlw(request.getNewFaAlw());
        entity.setNewTaAlw(request.getNewTaAlw());
        entity.setNewHraAlw(request.getNewHraAlw());
        entity.setNewFixotAlw(request.getNewFixotAlw());
        entity.setNewSplAlw(request.getNewSplAlw());
        entity.setNewOthAlw(request.getNewOthAlw());
        entity.setNewAvgot(request.getNewAvgot());
        entity.setNewGrossPay(request.getNewGrossPay());
        entity.setStatus(request.getStatus());
        entity.setNetIncrement(request.getNetIncrement());
        entity.setLastIncrementDate(request.getLastIncrementDate());
        entity.setLastIncrementAmt(request.getLastIncrementAmt());
        entity.setLastBonus(request.getLastBonus());
        entity.setCurTicketPeriod(request.getCurTicketPeriod());
        entity.setCurNoOfTickets(request.getCurNoOfTickets());
        entity.setNewTicketPeriod(request.getNewTicketPeriod());
        entity.setNewNoOfTickets(request.getNewNoOfTickets());
        entity.setNewDesignationPoid(request.getNewDesignationPoid());
        entity.setArrears(request.getArrears());
        entity.setNewBonusPer(request.getNewBonusPer());
        entity.setLetterEmailedOn(request.getLetterEmailedOn());
        entity.setGridListingMethod(request.getGridListingMethod());
        entity.setRegisteredSalary(request.getRegisteredSalary());
        entity.setCurMonthlyCtc(request.getCurMonthlyCtc());
        entity.setCurYearlyCtc(request.getCurYearlyCtc());
        entity.setNewMonthlyCtc(request.getNewMonthlyCtc());
        entity.setNewYearlyCtc(request.getNewYearlyCtc());
        entity.setLastDesignationPoid(request.getLastDesignationPoid());
        entity.setLastPromotionDate(request.getLastPromotionDate());
    }

    private Map<String, Object> params(Object... values) {
        Map<String, Object> out = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            out.put(String.valueOf(values[i]), values[i + 1]);
        }
        return out;
    }

    /**
     * Legacy {@code HRAppraisalBean#DocumentBeforeSave} and header fields marked {@code required="true"}
     * on {@code AppraisalPage.jsff} (Description, Period From, second-tab appraisal percents).
     */
    private void validateHeaderForLegacySave(HrAppraisalRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is required.");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new ValidationException("Description is required.");
        }
        if (request.getPeriodFrom() == null) {
            throw new ValidationException("Effective date (period from) is required.");
        }
        if (request.getAppraisalBasicPercent() == null) {
            throw new ValidationException("Appraisal Basic Percent is required in the second tab...");
        }
        if (request.getAppraisalFixedPercent() == null) {
            throw new ValidationException("Appraisal Fixed Percent is required in the second tab...");
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
