package com.asg.payroll.payrollprocess.service;

import com.asg.common.lib.dto.*;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.service.PrintService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.payroll.common.util.ActionType;
import com.asg.payroll.employeeappraisal.entity.HrPayrollVarAlwdedDtl;
import com.asg.payroll.employeeappraisal.entity.HrPayrollVarAlwdedDtlId;
import com.asg.payroll.employeeappraisal.repository.HrPayrollVarAlwdedDtlRepository;
import com.asg.payroll.exceptions.ResourceNotFoundException;
import com.asg.payroll.exceptions.ValidationException;
import com.asg.payroll.payrollprocess.dto.HrPayrollHdrRequest;
import com.asg.payroll.payrollprocess.dto.HrPayrollRecurringDtlRequest;
import com.asg.payroll.payrollprocess.dto.HrPayrollVarAlwdedDtlRequest;
import com.asg.payroll.payrollprocess.dto.PayrollActionRequest;
import com.asg.payroll.payrollprocess.entity.*;
import com.asg.payroll.payrollprocess.repository.*;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Types;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HrPayrollProcessServiceImpl implements HrPayrollProcessService {

    private static final String PAYROLL_NOT_FOUND = "Payroll not found with ID: ";
    private static final String TRANSACTION_POID = "TRANSACTION_POID";
    private static final String P_STATUS = "P_STATUS";
    private static final String P_COMPANY_POID = "P_COMPANY_POID";
    private static final String P_TRANSACTION_POID = "P_TRANSACTION_POID";
    private static final String P_LOGIN_USER_POID = "P_LOGIN_USER_POID";
    private static final String P_ATTEND_TRAN_POID = "P_ATTEND_TRAN_POID";
    private static final String P_PAYROLL_MONTH = "P_PAYROLL_MONTH";
    private static final String P_SUPPRESS_ARREARS = "P_SUPPRESS_ARREARS";
    private static final String P_TRN_DATE = "P_TRN_DATE";
    private static final String P_ACTION_TYPE = "P_ACTION_TYPE";
    private static final String P_RESEND = "P_RESEND";
    private static final String P_SCHEDULE_ON = "P_SCHEDULE_ON";
    private static final String P_MODE = "P_MODE";
    private static final String P_FILE_NAME = "P_FILE_NAME";
    private static final String OUTDATA = "OUTDATA";

    private final EntityManager entityManager;
    private final HrPayrollProcessService self;
    private final HrPayrollHdrRepository hdrRepository;
    private final HrPayrollDtlRepository dtlRepository;
    private final HrPayrollProvisionDtlRepository provisionDtlRepository;
    private final HrPayrollRecurringDtlRepository recurringDtlRepository;
    private final HrPayrollVarAlwdedDtlRepository varAlwdedDtlRepository;
    private final DocumentSearchService documentSearchService;
    private final DocumentDeleteService documentDeleteService;
    private final LoggingService loggingService;
    private final LovDataService lovDataService;
    private final PrintService printService;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public HrPayrollProcessServiceImpl(
            EntityManager entityManager,
            @Lazy HrPayrollProcessService self,
            HrPayrollHdrRepository hdrRepository,
            HrPayrollDtlRepository dtlRepository,
            HrPayrollProvisionDtlRepository provisionDtlRepository,
            HrPayrollRecurringDtlRepository recurringDtlRepository,
            HrPayrollVarAlwdedDtlRepository varAlwdedDtlRepository,
            DocumentSearchService documentSearchService,
            DocumentDeleteService documentDeleteService,
            LoggingService loggingService,
            LovDataService lovDataService,
            PrintService printService,
            DataSource dataSource,
            JdbcTemplate jdbcTemplate) {
        this.entityManager = entityManager;
        this.self = self;
        this.hdrRepository = hdrRepository;
        this.dtlRepository = dtlRepository;
        this.provisionDtlRepository = provisionDtlRepository;
        this.recurringDtlRepository = recurringDtlRepository;
        this.varAlwdedDtlRepository = varAlwdedDtlRepository;
        this.documentSearchService = documentSearchService;
        this.documentDeleteService = documentDeleteService;
        this.loggingService = loggingService;
        this.lovDataService = lovDataService;
        this.printService = printService;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ─── LIST / GET ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listPayrolls(String documentId, FilterRequestDto filters, Pageable pageable,
                                            LocalDate periodFrom, LocalDate periodTo) {
        String operator = documentSearchService.resolveOperator(filters);
        String isDeleted = documentSearchService.resolveIsDeleted(filters);
        List<FilterDto> resolvedFilters = documentSearchService.resolveDateFilters(filters, "TRANSACTION_DATE", periodFrom, periodTo);
        RawSearchResult raw = documentSearchService.search(documentId, resolvedFilters, operator, pageable, isDeleted, "DOC_REF", TRANSACTION_POID);
        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());
        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPayrollById(Long transactionPoid) {
        HrPayrollHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));

        List<HrPayrollVarAlwdedDtl> varDetails = varAlwdedDtlRepository.findByTransactionPoid(transactionPoid);
        List<HrPayrollRecurringDtl> recurDetails = recurringDtlRepository.findByTransactionPoid(transactionPoid);

        // Collect unique POIDs across both detail lists for employee LOV
        Set<Long> empPoids = new HashSet<>();
        varDetails.stream().map(HrPayrollVarAlwdedDtl::getEmployeePoid).filter(Objects::nonNull).forEach(empPoids::add);
        recurDetails.stream().map(HrPayrollRecurringDtl::getEmployeePoid).filter(Objects::nonNull).forEach(empPoids::add);

        Set<Long> alwdedPoids = varDetails.stream()
                .map(HrPayrollVarAlwdedDtl::getAllowanceDeductionPoid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Object> out = new HashMap<>();
        out.put("header", hdr);
        out.put("payrollDetails", dtlRepository.findByTransactionPoid(transactionPoid));
        out.put("variableDetails", varDetails);
        out.put("provisionDetails", provisionDtlRepository.findByTransactionPoid(transactionPoid));
        out.put("recurringDetails", recurDetails);

        // LOV enrichment
        if (hdr.getAttendTranPoid() != null) {
            out.put("attendancePeriodLov", lovDataService.getDetailsByPoidAndLovName(
                    hdr.getAttendTranPoid(), "HR_ATTENDANCE_POID"));
        }
        if (!empPoids.isEmpty()) {
            out.put("employeeLov", empPoids.stream().collect(Collectors.toMap(
                    p -> p,
                    p -> lovDataService.getDetailsByPoidAndLovName(p, "EMPLOYEE_NAME"))));
        }
        if (!alwdedPoids.isEmpty()) {
            out.put("allowanceDeductionLov", alwdedPoids.stream().collect(Collectors.toMap(
                    p -> p,
                    p -> lovDataService.getDetailsByPoidAndLovName(p, "EMP_ALOW_DEDUCTION"))));
        }

        return out;
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @Override
    public Map<String, Object> createPayroll(HrPayrollHdrRequest request) {
        validateBeforeSave(request, null);
        HrPayrollHdr hdr = new HrPayrollHdr();
        mapHeader(request, hdr);
        hdr.setCompanyPoid(UserContext.getCompanyPoid());
        hdr.setGroupPoid(UserContext.getGroupPoid());
        hdr.setDeleted("N");
        HrPayrollHdr saved = hdrRepository.saveAndFlush(hdr);
        upsertVarDetails(saved.getTransactionPoid(), request.getVariableDetails(), true);
        upsertRecurringDetails(saved.getTransactionPoid(), request.getRecurringDetails(), true);
        entityManager.refresh(saved);
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), saved.getTransactionPoid().toString(),
                String.format("%s %s", LogDetailsEnum.CREATED.getDescription(), saved.getDocRef()));
        return self.getPayrollById(saved.getTransactionPoid());
    }

    @Override
    public Map<String, Object> updatePayroll(Long transactionPoid, HrPayrollHdrRequest request) {
        HrPayrollHdr existing = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));
        validateBeforeEdit(transactionPoid, existing);
        validateBeforeSave(request, transactionPoid);
        HrPayrollHdr oldEntity = new HrPayrollHdr();
        BeanUtils.copyProperties(existing, oldEntity);
        mapHeader(request, existing);
        hdrRepository.save(existing);
        upsertVarDetails(transactionPoid, request.getVariableDetails(), false);
        upsertRecurringDetails(transactionPoid, request.getRecurringDetails(), false);
        loggingService.logChanges(oldEntity, existing, HrPayrollHdr.class, UserContext.getDocumentId(),
                transactionPoid.toString(), LogDetailsEnum.MODIFIED, TRANSACTION_POID);
        return self.getPayrollById(transactionPoid);
    }

    @Override
    public void deletePayroll(Long transactionPoid, DeleteReasonDto deleteReasonDto) {
        HrPayrollHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));
        documentDeleteService.deleteDocument(transactionPoid, "HR_PAYROLL_HDR", TRANSACTION_POID,
                deleteReasonDto, hdr.getTransactionDate());
    }

    // ─── PROCEDURE ACTIONS ───────────────────────────────────────────────────

    @Override
    public Map<String, Object> processPayroll(Long transactionPoid, PayrollActionRequest request) {
        validateActionRequest(transactionPoid, request);
        String suppressArrears = Boolean.TRUE.equals(request.getSuppressArrearsValidation()) ? "Y" : "N";
        Map<String, Object> result = execute(
                "PROC_HR_PAYROLL_PROCESS",
                List.of(
                        new SqlParameter(P_COMPANY_POID, Types.NUMERIC),
                        new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC),
                        new SqlParameter("P_EMP_POID", Types.NUMERIC),
                        new SqlParameter(P_ATTEND_TRAN_POID, Types.NUMERIC),
                        new SqlParameter("P_DEPT_POID", Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_MONTH, Types.DATE),
                        new SqlParameter("P_ADDITIONAL", Types.VARCHAR),
                        new SqlParameter(P_SUPPRESS_ARREARS, Types.VARCHAR),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(
                        P_COMPANY_POID, UserContext.getCompanyPoid(),
                        P_TRANSACTION_POID, transactionPoid,
                        "P_EMP_POID", null,
                        P_ATTEND_TRAN_POID, request.getAttendTranPoid(),
                        "P_DEPT_POID", null,
                        P_PAYROLL_MONTH, request.getPayrollMonth(),
                        "P_ADDITIONAL", null,
                        P_SUPPRESS_ARREARS, suppressArrears
                )
        );
        logProcedureResult(transactionPoid, result, "Payroll processed...", "Payroll processing completed with warning.");
        return result;
    }

    @Override
    public Map<String, Object> processProvision(Long transactionPoid, PayrollActionRequest request) {
        validateActionRequest(transactionPoid, request);
        Map<String, Object> result = execute(
                "PROC_HR_PAYROLL_PROVISION",
                List.of(
                        new SqlParameter(P_COMPANY_POID, Types.NUMERIC),
                        new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC),
                        new SqlParameter("P_EMP_POID", Types.NUMERIC),
                        new SqlParameter(P_ATTEND_TRAN_POID, Types.NUMERIC),
                        new SqlParameter("P_DEPT_POID", Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_MONTH, Types.DATE),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR),
                        new SqlParameter("P_CREATE_JV", Types.VARCHAR),
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC)
                ),
                params(
                        P_COMPANY_POID, UserContext.getCompanyPoid(),
                        P_TRANSACTION_POID, transactionPoid,
                        "P_EMP_POID", null,
                        P_ATTEND_TRAN_POID, request.getAttendTranPoid(),
                        "P_DEPT_POID", null,
                        P_PAYROLL_MONTH, request.getPayrollMonth(),
                        "P_CREATE_JV", "Y",
                        P_LOGIN_USER_POID, UserContext.getUserPoid()
                )
        );
        logProcedureResult(transactionPoid, result, "Provision processed...", "Provision processing completed with warning.");
        return result;
    }

    @Override
    public Map<String, Object> revertPayroll(Long transactionPoid) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));
        Map<String, Object> result = execute(
                "PROC_HR_PAYROLL_DELETE",
                List.of(
                        new SqlParameter(P_COMPANY_POID, Types.NUMERIC),
                        new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(P_COMPANY_POID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid)
        );
        logProcedureResult(transactionPoid, result, "Payroll reverted / cancelled...", "Payroll revert completed with warning.");
        return result;
    }

    @Override
    public Map<String, Object> loadVariables(Long transactionPoid, PayrollActionRequest request) {
        if (request.getPayrollMonth() == null) {
            throw new ValidationException("Payroll month is required to load variables.");
        }
        Map<String, Object> result = execute(
                "PROC_HR_VARIABLE_TO_PAYROLL",
                List.of(
                        new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC),
                        new SqlParameter("P_EMP_POID", Types.NUMERIC),
                        new SqlParameter("P_DEPT_POID", Types.NUMERIC),
                        new SqlParameter(P_TRN_DATE, Types.VARCHAR),
                        new SqlOutParameter(OUTDATA, OracleTypes.CURSOR),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(
                        P_TRANSACTION_POID, transactionPoid,
                        "P_EMP_POID", null,
                        "P_DEPT_POID", null,
                        P_TRN_DATE, request.getPayrollMonth().toString()
                )
        );
        logProcedureResult(transactionPoid, result, "Payroll variables refreshed...", null);
        return result;
    }

    @Override
    public Map<String, Object> loadLoansAdvances(Long transactionPoid, PayrollActionRequest request) {
        if (request.getPayrollMonth() == null) {
            throw new ValidationException("Payroll month is required to load loans and advances.");
        }
        Map<String, Object> result = execute(
                "PROC_HR_RECURRING_TO_PAYROLL",
                List.of(
                        new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC),
                        new SqlParameter("P_EMP_POID", Types.NUMERIC),
                        new SqlParameter("P_DEPT_POID", Types.NUMERIC),
                        new SqlParameter(P_TRN_DATE, Types.VARCHAR),
                        new SqlOutParameter(OUTDATA, OracleTypes.CURSOR)
                ),
                params(
                        P_TRANSACTION_POID, transactionPoid,
                        "P_EMP_POID", null,
                        "P_DEPT_POID", null,
                        P_TRN_DATE, request.getPayrollMonth().toString()
                )
        );
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                "Loans and Advances / Recurring Deductions refreshed...");
        return result;
    }

    @Override
    public Map<String, Object> createJv(Long transactionPoid) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));

        Map<String, Object> jvResult = execute(
                "PROC_HR_PAYROLL_CREATE_JV",
                List.of(
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC),
                        new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(P_LOGIN_USER_POID, UserContext.getUserPoid(), P_TRANSACTION_POID, transactionPoid)
        );

        Map<String, Object> provJvResult = execute(
                "PROC_HR_PAYROLL_CREATE_PROV_JV",
                List.of(
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC),
                        new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(P_LOGIN_USER_POID, UserContext.getUserPoid(), P_TRANSACTION_POID, transactionPoid)
        );

        Map<String, Object> bdvResult = execute(
                "PROC_HR_PAYROLL_CREATE_BDV",
                List.of(
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC),
                        new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(P_LOGIN_USER_POID, UserContext.getUserPoid(), P_TRANSACTION_POID, transactionPoid)
        );

        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                "JV and Bank DV created...");
        loggingService.createLogDetailsEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                "PAYROLL_JV_STATUS", null, String.valueOf(jvResult.get(P_STATUS)),
                "Create JV", "HR_PAYROLL_HDR");
        loggingService.createLogDetailsEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                "PROV_JV_STATUS", null, String.valueOf(provJvResult.get(P_STATUS)),
                "Create Provision JV", "HR_PAYROLL_HDR");
        loggingService.createLogDetailsEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                "BANK_DV_STATUS", null, String.valueOf(bdvResult.get(P_STATUS)),
                "Create Bank DV", "HR_PAYROLL_HDR");

        Map<String, Object> out = new HashMap<>();
        out.put("payrollJvStatus", jvResult.get(P_STATUS));
        out.put("provisionJvStatus", provJvResult.get(P_STATUS));
        out.put("bankDvStatus", bdvResult.get(P_STATUS));
        return out;
    }

    @Override
    public Map<String, Object> generateBankFile(Long transactionPoid) {
        return executeBankFileGeneration(transactionPoid, "FILE");
    }

    @Override
    public Map<String, Object> hsbcApiTransfer(Long transactionPoid) {
        return executeBankFileGeneration(transactionPoid, "API");
    }

    @Override
    public Map<String, Object> syncHrData() {
        Map<String, Object> result = execute(
                "SYNC_HR_PRODUCTION_TO_PAYROLL",
                List.of(new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params()
        );
        String status = (String) result.get(P_STATUS);
        String logMsg = (status != null) ? "Sync HR Data: " + status : "Sync HR Data executed...";
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), null, logMsg);
        return result;
    }

    @Override
    public Map<String, Object> sendEmail(Long transactionPoid, PayrollActionRequest request) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));
        if (request.getScheduleOn() == null) {
            throw new ValidationException("Please enter a payslip schedule time.");
        }
        String resend = request.getResend() != null ? request.getResend() : "N";
        Map<String, Object> result = execute(
                "PROC_PAYROLL_SEND_EMAIL",
                List.of(
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC),
                        new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC),
                        new SqlParameter(P_RESEND, Types.VARCHAR),
                        new SqlParameter(P_SCHEDULE_ON, Types.TIMESTAMP),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(
                        P_LOGIN_USER_POID, UserContext.getUserPoid(),
                        P_TRANSACTION_POID, transactionPoid,
                        P_RESEND, resend,
                        P_SCHEDULE_ON, request.getScheduleOn()
                )
        );
        logProcedureResult(transactionPoid, result,
                String.format("Payslip email scheduled on %s (resend=%s).", request.getScheduleOn(), resend),
                null);
        return result;
    }

    // ─── PRINT ───────────────────────────────────────────────────────────────

    @Override
    public byte[] printPayslip(Long transactionPoid) throws JRException {
        return generatePdf("HR/Payslip_without_email.jrxml", transactionPoid);
    }

    @Override
    public byte[] printPreview(Long transactionPoid) throws JRException {
        return generatePdf("HR/Payslip.jrxml", transactionPoid);
    }

    private byte[] generatePdf(String reportFile, Long transactionPoid) throws JRException {
        Map<String, Object> params = printService.buildBaseParams(transactionPoid, UserContext.getDocumentId());
        params.put(TRANSACTION_POID, transactionPoid);
        JasperReport report = printService.load(reportFile);
        try {
            return printService.fillReportToPdf(report, params, dataSource);
        } catch (JRException e) {
            throw e;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    // ─── PRIVATE HELPERS ─────────────────────────────────────────────────────

    private Map<String, Object> executeBankFileGeneration(Long transactionPoid, String mode) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));
        Map<String, Object> result = execute(
                "PROC_BANK_FILE_GENERATION",
                List.of(
                        new SqlParameter(P_COMPANY_POID, Types.NUMERIC),
                        new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC),
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC),
                        new SqlOutParameter(P_FILE_NAME, Types.VARCHAR),
                        new SqlParameter(P_MODE, Types.VARCHAR)
                ),
                params(
                        P_COMPANY_POID, UserContext.getCompanyPoid(),
                        P_TRANSACTION_POID, transactionPoid,
                        P_LOGIN_USER_POID, UserContext.getUserPoid(),
                        P_MODE, mode
                )
        );
        String logMsg = "FILE".equals(mode) ? "Payroll bank file generated..." : "HSBC API transfer initiated...";
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logMsg);
        return result;
    }

    /**
     * Writes a summary log after a stored-procedure call.
     * Skips on ERROR; uses successMsg on clean result, warningMsg (if set) on WARNING.
     */
    private void logProcedureResult(Long transactionPoid, Map<String, Object> result,
                                    String successMsg, String warningMsg) {
        String status = (String) result.get(P_STATUS);
        if (status != null && status.toUpperCase().contains("ERROR")) {
            return;
        }
        String msg = (status != null && status.toUpperCase().contains("WARNING") && warningMsg != null)
                ? warningMsg : successMsg;
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), msg);
    }

    // ─── VALIDATION ──────────────────────────────────────────────────────────

    private void validateBeforeSave(HrPayrollHdrRequest request, Long existingPoid) {
        if (request.getPayrollMonth() == null) {
            throw new ValidationException("Payroll date is a required field.");
        }
        LocalDate payrollMonth = request.getPayrollMonth();
        LocalDate monthEnd = YearMonth.from(payrollMonth).atEndOfMonth();
        if (!payrollMonth.equals(monthEnd)) {
            throw new ValidationException("Payroll date has to be month end date.");
        }
        callValidateProc(existingPoid, request.getAttendTranPoid(), payrollMonth, "BEFORE_SAVE");
    }

    private void validateBeforeEdit(Long transactionPoid, HrPayrollHdr existing) {
        callValidateProc(transactionPoid, existing.getAttendTranPoid(), existing.getPayrollMonth(), "BEFORE_EDIT");
    }

    private void callValidateProc(Long docPoid, Long attendTranPoid, LocalDate payrollDate, String actionType) {
        Map<String, Object> result = execute(
                "PROC_HR_PAYROLL_VALIDATE",
                List.of(
                        new SqlParameter("P_DOC_POID", Types.NUMERIC),
                        new SqlParameter("P_ATTEND_TRAN_POID", Types.NUMERIC),
                        new SqlParameter("P_PAYROLL_DATE", Types.DATE),
                        new SqlParameter(P_ACTION_TYPE, Types.VARCHAR),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(
                        "P_DOC_POID", docPoid,
                        "P_ATTEND_TRAN_POID", attendTranPoid,
                        "P_PAYROLL_DATE", payrollDate,
                        P_ACTION_TYPE, actionType
                )
        );
        String status = (String) result.get(P_STATUS);
        if (status != null && status.toUpperCase().contains("ERROR")) {
            throw new ValidationException(status);
        }
    }

    private void validateActionRequest(Long transactionPoid, PayrollActionRequest request) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));
        if (request.getAttendTranPoid() == null) {
            throw new ValidationException("Attendance monthly data not selected or invalid.");
        }
        if (request.getPayrollMonth() == null) {
            throw new ValidationException("Payroll month is invalid.");
        }
    }

    // ─── HEADER MAPPING ──────────────────────────────────────────────────────

    private void mapHeader(HrPayrollHdrRequest request, HrPayrollHdr entity) {
        entity.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now());
        entity.setAttendTranPoid(request.getAttendTranPoid());
        entity.setPayrollMonth(request.getPayrollMonth());
        entity.setAttendancePeriodDesc(request.getAttendancePeriodDesc());
        entity.setBankTransferValueDate(request.getBankTransferValueDate());
        entity.setEmailPayslipScheduleOn(request.getEmailPayslipScheduleOn());
        entity.setSuppressArrearsValidation(request.getSuppressArrearsValidation() != null
                ? request.getSuppressArrearsValidation() : "N");
    }

    // ─── VARIABLE ALLOWANCE / DEDUCTION DETAILS ──────────────────────────────

    private void upsertVarDetails(Long transactionPoid, List<HrPayrollVarAlwdedDtlRequest> requests, boolean createMode) {
        if (requests == null || requests.isEmpty()) return;
        for (HrPayrollVarAlwdedDtlRequest req : requests) {
            ActionType action = createMode && req.getActionType() == null ? ActionType.ISCREATED : req.getActionType();
            if (action == null || action == ActionType.NOCHANGE) continue;
            switch (action) {
                case ISCREATED -> createVarDetail(transactionPoid, req);
                case ISDELETED -> deleteVarDetail(transactionPoid, req.getDetRowId());
                default -> updateVarDetail(transactionPoid, req);
            }
        }
    }

    private void createVarDetail(Long transactionPoid, HrPayrollVarAlwdedDtlRequest req) {
        long nextRowId = varAlwdedDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
        HrPayrollVarAlwdedDtl entity = new HrPayrollVarAlwdedDtl();
        entity.setTransactionPoid(transactionPoid);
        entity.setDetRowId(nextRowId);
        mapVarDetail(req, entity);
        varAlwdedDtlRepository.save(entity);
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                String.format("Row Created on [Payroll Variable Details] with DetRowId: %d", nextRowId));
    }

    private void updateVarDetail(Long transactionPoid, HrPayrollVarAlwdedDtlRequest req) {
        HrPayrollVarAlwdedDtlId id = new HrPayrollVarAlwdedDtlId(req.getDetRowId(), transactionPoid);
        HrPayrollVarAlwdedDtl existing = varAlwdedDtlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variable detail not found for detRowId: " + req.getDetRowId()));
        HrPayrollVarAlwdedDtl snapshot = new HrPayrollVarAlwdedDtl();
        BeanUtils.copyProperties(existing, snapshot);
        mapVarDetail(req, existing);
        varAlwdedDtlRepository.save(existing);
        loggingService.createLog(snapshot, existing, HrPayrollVarAlwdedDtl.class,
                UserContext.getDocumentId(), transactionPoid.toString(),
                String.format("KeyId = TRANSACTION_POID %d: DET_ROW_ID %d", transactionPoid, req.getDetRowId()));
    }

    private void deleteVarDetail(Long transactionPoid, Long detRowId) {
        HrPayrollVarAlwdedDtlId id = new HrPayrollVarAlwdedDtlId(detRowId, transactionPoid);
        HrPayrollVarAlwdedDtl entity = varAlwdedDtlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variable detail not found for detRowId: " + detRowId));
        varAlwdedDtlRepository.deleteById(id);
        loggingService.logDelete(entity, UserContext.getDocumentId(), transactionPoid.toString());
    }

    private void mapVarDetail(HrPayrollVarAlwdedDtlRequest req, HrPayrollVarAlwdedDtl entity) {
        entity.setEmployeePoid(req.getEmployeePoid());
        entity.setAllowanceDeductionPoid(req.getAllowanceDeductionPoid());
        entity.setAmount(req.getAmount());
        entity.setRemarks(req.getRemarks());
    }

    // ─── RECURRING DETAIL (LOAN / ADVANCE) ───────────────────────────────────

    private void upsertRecurringDetails(Long transactionPoid, List<HrPayrollRecurringDtlRequest> requests, boolean createMode) {
        if (requests == null || requests.isEmpty()) return;
        for (HrPayrollRecurringDtlRequest req : requests) {
            ActionType action = createMode && req.getActionType() == null ? ActionType.ISCREATED : req.getActionType();
            if (action == null || action == ActionType.NOCHANGE) continue;
            switch (action) {
                case ISCREATED -> createRecurringDetail(transactionPoid, req);
                case ISDELETED -> deleteRecurringDetail(transactionPoid, req.getDetRowId());
                default -> updateRecurringDetail(transactionPoid, req);
            }
        }
    }

    private void createRecurringDetail(Long transactionPoid, HrPayrollRecurringDtlRequest req) {
        long nextRowId = recurringDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
        HrPayrollRecurringDtl entity = new HrPayrollRecurringDtl();
        entity.setTransactionPoid(transactionPoid);
        entity.setDetRowId(nextRowId);
        mapRecurringDetail(req, entity);
        recurringDtlRepository.save(entity);
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                String.format("Row Created on [Payroll Recurring Details] with DetRowId: %d", nextRowId));
    }

    private void updateRecurringDetail(Long transactionPoid, HrPayrollRecurringDtlRequest req) {
        HrPayrollRecurringDtlId id = new HrPayrollRecurringDtlId(transactionPoid, req.getDetRowId());
        HrPayrollRecurringDtl existing = recurringDtlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring detail not found for detRowId: " + req.getDetRowId()));
        HrPayrollRecurringDtl snapshot = new HrPayrollRecurringDtl();
        BeanUtils.copyProperties(existing, snapshot);
        mapRecurringDetail(req, existing);
        recurringDtlRepository.save(existing);
        loggingService.createLog(snapshot, existing, HrPayrollRecurringDtl.class,
                UserContext.getDocumentId(), transactionPoid.toString(),
                String.format("KeyId = TRANSACTION_POID %d: DET_ROW_ID %d", transactionPoid, req.getDetRowId()));
    }

    private void deleteRecurringDetail(Long transactionPoid, Long detRowId) {
        HrPayrollRecurringDtlId id = new HrPayrollRecurringDtlId(transactionPoid, detRowId);
        HrPayrollRecurringDtl entity = recurringDtlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring detail not found for detRowId: " + detRowId));
        recurringDtlRepository.deleteById(id);
        loggingService.logDelete(entity, UserContext.getDocumentId(), transactionPoid.toString());
    }

    private void mapRecurringDetail(HrPayrollRecurringDtlRequest req, HrPayrollRecurringDtl entity) {
        entity.setEmployeePoid(req.getEmployeePoid());
        entity.setRefNo(req.getRefNo());
        entity.setBalAmt(req.getBalAmt());
        entity.setRecurAmount(req.getRecurAmount());
        entity.setRemarks(req.getRemarks());
    }

    // ─── JDBC HELPERS ────────────────────────────────────────────────────────

    private Map<String, Object> execute(String procedureName, List<SqlParameter> parameters,
                                        Map<String, Object> inParams) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName(procedureName)
                .declareParameters(parameters.toArray(new SqlParameter[0]));
        return call.execute(inParams);
    }

    private Map<String, Object> params(Object... values) {
        Map<String, Object> out = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            out.put(String.valueOf(values[i]), values[i + 1]);
        }
        return out;
    }
}
