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
import com.asg.payroll.payrollprocess.dto.*;
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
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HrPayrollProcessServiceImpl implements HrPayrollProcessService {

    private static final String PAYROLL_NOT_FOUND = "Payroll not found with ID: ";
    private static final String TRANSACTION_POID = "TRANSACTION_POID";
    private static final String P_STATUS = "P_STATUS";
    private static final String P_LOGIN_USER_POID = "P_LOGIN_USER_POID";
    private static final String P_RESEND = "P_RESEND";
    
    // Additional constants for duplicated strings
    private static final String TRANSACTION_DATE = "TRANSACTION_DATE";
    private static final String DOC_REF = "DOC_REF";
    private static final String HR_ATTENDANCE_POID = "HR_ATTENDANCE_POID";
    private static final String EMPLOYEE_NAME = "EMPLOYEE_NAME";
    private static final String EMP_ALOW_DEDUCTION = "EMP_ALOW_DEDUCTION";
    private static final String HR_PAYROLL_HDR = "HR_PAYROLL_HDR";
    private static final String P_COMPANYID = "P_COMPANYID";
    private static final String P_PAYROLL_TRANS_POID = "P_PAYROLL_TRANS_POID";
    private static final String P_SETTLEMENT_TRAN_POID = "P_SETTLEMENT_TRAN_POID";
    private static final String P_ATTEND_TRNS_ID = "P_ATTEND_TRNS_ID";
    private static final String P_EMPPOID = "P_EMPPOID";
    private static final String P_PAYROLL_DATE = "P_PAYROLL_DATE";
    private static final String P_LOAN_DED_AMT = "P_LOAN_DED_AMT";
    private static final String P_SUPPRESS_ARREARS_VALIDATION = "P_SUPPRESS_ARREARS_VALIDATION";
    private static final String P_POST_JV = "P_POST_JV";
    private static final String P_PAYROLL_POID = "P_PAYROLL_POID";
    private static final String VARIABLES_REC = "VARIABLES_REC";
    private static final String P_BANK_CASH = "P_BANK_CASH";
    private static final String P_TRNNO = "P_TRNNO";
    private static final String P_APIFILE = "p_apifile";
    private static final String BANK = "BANK";
    private static final String FILE = "FILE";
    private static final String API = "API";
    private static final String P_PAYSLIP_SCHEDULE_TIME = "P_PAYSLIP_SCHEDULE_TIME";
    private static final String N = "N";
    private static final String Y = "Y";
    private static final String ERROR = "ERROR";
    private static final String WARNING = "WARNING";
    private static final String EMPLOYEE_POID = "EMPLOYEE_POID";
    private static final String ALLOWANCE_DEDUCTION_POID = "ALLOWANCE_DEDUCTION_POID";
    private static final String MONTHLY_WOKING_DAYS = "MONTHLY_WOKING_DAYS";
    private static final String ATTENDANCE_FROM = "ATTENDANCE_FROM";
    private static final String ATTENDANCE_TO = "ATTENDANCE_TO";
    private static final String P_ATTENDANCE_POID = "P_ATTENDANCE_POID";
    private static final String P_VALIDATE_ACTION = "P_VALIDATE_ACTION";
    private static final String BEFORE_SAVE = "BEFORE_SAVE";
    private static final String BEFORE_EDIT = "BEFORE_EDIT";
    private static final String P_COMPANY_POID = "P_COMPANY_POID";
    private static final String P_FILE_NAME = "P_FILE_NAME";
    
    // Additional constants for validation methods
    private static final String MORE_EMPLOYEES_MSG = "• ... and ";
    private static final String MORE_EMPLOYEES_SUFFIX = " more employees\n";
    private static final String ID_PREFIX = " (ID: ";
    private static final String ID_SUFFIX = ")";
    private static final String EMPLOYEE_ID_PREFIX = "Employee ID: ";
    private static final String DISCONTINUED_DATE = "DISCONTINUED_DATE";
    private static final String JOIN_DATE = "JOIN_DATE";
    private static final String ACTIVE = "ACTIVE";
    private static final String DISCONTINUED = "DISCONTINUED";
    private static final String NULL_STRING = "null";
    private static final String DISCONTINUED_SUFFIX = ", Discontinued: ";

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
        List<FilterDto> resolvedFilters = documentSearchService.resolveDateFilters(filters, TRANSACTION_DATE, periodFrom, periodTo);
        RawSearchResult raw = documentSearchService.search(documentId, resolvedFilters, operator, pageable, isDeleted, DOC_REF, TRANSACTION_POID);
        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());
        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    @Transactional(readOnly = true)
    public HrPayrollHdrResponse getPayrollById(Long transactionPoid) {
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

        HrPayrollHdrResponse response = new HrPayrollHdrResponse();
        
        // Map header fields
        response.setTransactionPoid(hdr.getTransactionPoid());
        response.setDocRef(hdr.getDocRef());
        response.setTransactionDate(hdr.getTransactionDate());
        response.setAttendTranPoid(hdr.getAttendTranPoid());
        response.setPayrollMonth(hdr.getPayrollMonth());
        response.setAttendancePeriodDesc(hdr.getAttendancePeriodDesc());
        response.setSuppressArrearsValidation(hdr.getSuppressArrearsValidation());
        response.setPayrollJvDocRef(hdr.getPayrollJvDocRef());
        response.setProvJvDocRef(hdr.getProvJvDocRef());
        response.setBankTransferDocRef(hdr.getBankTransferDocRef());
        response.setBankTransferValueDate(hdr.getBankTransferValueDate());
        response.setEmailPayslipScheduleOn(hdr.getEmailPayslipScheduleOn());
        response.setEmailPayslip(hdr.getEmailPayslip());
        response.setEmailPayslipCompletedOn(hdr.getEmailPayslipCompletedOn());
        response.setVerified(hdr.getVerified());
        response.setApproved(hdr.getApproved());
        response.setPayrollReleased(hdr.getPayrollReleased());
        
        // Set detail lists (these would need proper mapping if entities differ from DTOs)
        response.setPayrollDetails(mapToPayrollDtlResponse(dtlRepository.findByTransactionPoid(transactionPoid)));
        response.setVariableDetails(mapToVarAlwdedDtlResponse(varDetails));
        response.setProvisionDetails(mapToProvisionDtlResponse(provisionDtlRepository.findByTransactionPoid(transactionPoid)));
        response.setRecurringDetails(mapToRecurringDtlResponse(recurDetails));

        // LOV enrichment
        if (hdr.getAttendTranPoid() != null) {
            response.setAttendancePeriodLov(lovDataService.getDetailsByPoidAndLovName(
                    hdr.getAttendTranPoid(), HR_ATTENDANCE_POID));
        }
        if(null != response.getProvisionDetails()) {
            response.getProvisionDetails().stream().map(HrPayrollProvisionDtlResponse::getEmployeePoid).filter(Objects::nonNull).forEach(empPoids::add);
        }
        if(null != response.getPayrollDetails()) {
            response.getPayrollDetails().stream().map(HrPayrollDtlResponse::getEmployeePoid).filter(Objects::nonNull).forEach(empPoids::add);
        }
        if (!empPoids.isEmpty()) {
            response.setEmployeeLov(empPoids.stream().collect(Collectors.toMap(
                    p -> p,
                    p -> lovDataService.getDetailsByPoidAndLovName(p, EMPLOYEE_NAME))));
        }
        if (!alwdedPoids.isEmpty()) {
            response.setAllowanceDeductionLov(alwdedPoids.stream().collect(Collectors.toMap(
                    p -> p,
                    p -> lovDataService.getDetailsByPoidAndLovName(p, EMP_ALOW_DEDUCTION))));
        }
        mapLovFields(response);

        // Determine whether this payroll can still be edited (BEFORE_EDIT check).
        // We surface the proc message instead of throwing, so the UI can enable/disable
        // editing and show the reason.
        String editStatus = runValidateProc(
                hdr.getTransactionPoid(), hdr.getAttendTranPoid(), hdr.getPayrollMonth(), BEFORE_EDIT);
        boolean allowEdit = editStatus == null || !editStatus.toUpperCase().contains(ERROR);
        response.setAllowEdit(allowEdit);
        response.setInfoMessage(editStatus);

        return response;
    }

    private void mapLovFields(HrPayrollHdrResponse response) {
        // Map LOV fields for each detail list
        if (response.getPayrollDetails() != null) {
            response.getPayrollDetails().forEach(dtl -> {
                if (dtl.getEmployeePoid() != null) {
                    dtl.setEmployeeLov(response.getEmployeeLov().get(dtl.getEmployeePoid()));
                }
            });
        }
        if (response.getVariableDetails() != null) {
            response.getVariableDetails().forEach(varDtl -> {
                if (varDtl.getEmployeePoid() != null) {
                    varDtl.setEmployeeLov(response.getEmployeeLov().get(varDtl.getEmployeePoid()));
                }
                if (varDtl.getAllowanceDeductionPoid() != null) {
                    varDtl.setAllowanceDeductionLov(response.getAllowanceDeductionLov().get(varDtl.getAllowanceDeductionPoid()));
                }
            });
        }
        if (response.getProvisionDetails() != null) {
            response.getProvisionDetails().forEach(provDtl -> {
                if (provDtl.getEmployeePoid() != null) {
                    provDtl.setEmployeeLov(response.getEmployeeLov().get(provDtl.getEmployeePoid()));
                }
            });
        }
        if (response.getRecurringDetails() != null) {
            response.getRecurringDetails().forEach(recurDtl -> {
                if (recurDtl.getEmployeePoid() != null) {
                    recurDtl.setEmployeeLov(response.getEmployeeLov().get(recurDtl.getEmployeePoid()));
                }
            });
        }
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @Override
    public HrPayrollHdrResponse createPayroll(HrPayrollHdrRequest request) {
        validateBeforeSave(request, null);
        HrPayrollHdr hdr = new HrPayrollHdr();
        mapHeader(request, hdr);
        hdr.setCompanyPoid(UserContext.getCompanyPoid());
        hdr.setGroupPoid(UserContext.getGroupPoid());
        hdr.setDeleted(N);
        HrPayrollHdr saved = hdrRepository.saveAndFlush(hdr);
        entityManager.refresh(saved);
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), saved.getTransactionPoid().toString(),
                String.format("%s %s", LogDetailsEnum.CREATED.getDescription(), saved.getDocRef()));
        upsertVarDetails(saved.getTransactionPoid(), request.getVariableDetails(), true);
        upsertRecurringDetails(saved.getTransactionPoid(), request.getRecurringDetails(), true);
        return self.getPayrollById(saved.getTransactionPoid());
    }

    @Override
    public HrPayrollHdrResponse updatePayroll(Long transactionPoid, HrPayrollHdrRequest request) {
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
        documentDeleteService.deleteDocument(transactionPoid, HR_PAYROLL_HDR, TRANSACTION_POID,
                deleteReasonDto, hdr.getTransactionDate());
    }

    // ─── PROCEDURE ACTIONS ───────────────────────────────────────────────────

    @Override
    public PayrollActionResponse processPayroll(Long transactionPoid, PayrollActionRequest request) {
        validateActionRequest(transactionPoid, request);
        String suppressArrears = Boolean.TRUE.equals(request.getSuppressArrearsValidation()) ? Y : N;
        Map<String, Object> result = execute(
                "PROC_HR_PAYROLL_PROCESS",
                List.of(
                        new SqlParameter(P_COMPANYID, Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_TRANS_POID, Types.NUMERIC),
                        new SqlParameter(P_SETTLEMENT_TRAN_POID, Types.NUMERIC),
                        new SqlParameter(P_ATTEND_TRNS_ID, Types.NUMERIC),
                        new SqlParameter(P_EMPPOID, Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_DATE, Types.DATE),
                        new SqlParameter(P_LOAN_DED_AMT, Types.NUMERIC),
                        new SqlParameter(P_SUPPRESS_ARREARS_VALIDATION, Types.VARCHAR),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(
                        P_COMPANYID, UserContext.getCompanyPoid(),
                        P_PAYROLL_TRANS_POID, transactionPoid,
                        P_SETTLEMENT_TRAN_POID, request.getSettlementTranPoid(),
                        P_ATTEND_TRNS_ID, request.getAttendTranPoid(),
                        P_EMPPOID, request.getEmpPoid(),
                        P_PAYROLL_DATE, request.getPayrollMonth(),
                        P_LOAN_DED_AMT, request.getLoanDedAmt(),
                        P_SUPPRESS_ARREARS_VALIDATION, suppressArrears
                )
        );
        logProcedureResult(transactionPoid, result, "Payroll processed...", "Payroll processing completed with warning.");
        return new PayrollActionResponse((String) result.get(P_STATUS), "Payroll processing completed");
    }

    @Override
    public PayrollActionResponse processProvision(Long transactionPoid, PayrollActionRequest request, String postJv) {
        validateActionRequest(transactionPoid, request);
        Map<String, Object> result = execute(
                "PROC_HR_PAYROLL_PROVISION",
                List.of(
                        new SqlParameter(P_COMPANYID, Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_TRANS_POID, Types.NUMERIC),
                        new SqlParameter(P_SETTLEMENT_TRAN_POID, Types.NUMERIC),
                        new SqlParameter(P_ATTEND_TRNS_ID, Types.NUMERIC),
                        new SqlParameter(P_EMPPOID, Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_DATE, Types.DATE),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR),
                        new SqlParameter(P_POST_JV, Types.VARCHAR),
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC)
                ),
                params(
                        P_COMPANYID, UserContext.getCompanyPoid(),
                        P_PAYROLL_TRANS_POID, transactionPoid,
                        P_SETTLEMENT_TRAN_POID, request.getSettlementTranPoid(),
                        P_ATTEND_TRNS_ID, request.getAttendTranPoid(),
                        P_EMPPOID, request.getEmpPoid(),
                        P_PAYROLL_DATE, request.getPayrollMonth(),
                        P_POST_JV, postJv,
                        P_LOGIN_USER_POID, UserContext.getUserPoid()
                )
        );
        logProcedureResult(transactionPoid, result, "Provision processed...", "Provision processing completed with warning.");
        return new PayrollActionResponse((String) result.get(P_STATUS), "Provision processing completed");
    }

    @Override
    public PayrollActionResponse revertPayroll(Long transactionPoid) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));
        Map<String, Object> result = execute(
                "PROC_HR_PAYROLL_DELETE",
                List.of(
                        new SqlParameter(P_COMPANYID, Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_TRANS_POID, Types.NUMERIC),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_PAYROLL_TRANS_POID, transactionPoid)
        );
        logProcedureResult(transactionPoid, result, "Payroll reverted / cancelled...", "Payroll revert completed with warning.");
        return new PayrollActionResponse((String) result.get(P_STATUS), "Payroll revert completed");
    }

    @Override
    public VariableLoadResponse loadVariables(Long transactionPoid, Long settlementPoid, Long empPoid, String payrollDate) {
        LocalDate parsedDate = (payrollDate == null || payrollDate.isBlank()) ? null : LocalDate.parse(payrollDate);

        Map<String, Object> result = jdbcTemplate.execute((ConnectionCallback<Map<String, Object>>) connection -> {
            try (CallableStatement cs = connection.prepareCall("{call PROC_HR_VARIABLE_TO_PAYROLL(?,?,?,?,?,?)}")) {
                setNumeric(cs, 1, transactionPoid);
                setNumeric(cs, 2, settlementPoid);
                setNumeric(cs, 3, empPoid);
                setDate(cs, 4, parsedDate);
                cs.registerOutParameter(5, OracleTypes.CURSOR);
                cs.registerOutParameter(6, Types.VARCHAR);
                cs.execute();

                Map<String, Object> out = new HashMap<>();
                out.put(VARIABLES_REC, extractCursor(cs.getObject(5)));
                out.put(P_STATUS, cs.getString(6));
                return out;
            }
        });
        logProcedureResult(transactionPoid, result, "Payroll variables loaded...", null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> variables = (List<Map<String, Object>>) result.get(VARIABLES_REC);

        Set<Long> empPoids = variables.stream()
                .map(map -> map.get(EMPLOYEE_POID))
                .filter(Objects::nonNull)
                .map(value -> ((Number) value).longValue())
                .collect(Collectors.toSet());

        Set<Long> alwdedPoids = variables.stream()
                .map(map -> map.get(ALLOWANCE_DEDUCTION_POID))
                .filter(Objects::nonNull)
                .map(value -> ((Number) value).longValue())
                .collect(Collectors.toSet());

        // Collectors.toMap rejects null values; the LOV lookup may return null
        // for a poid with no master entry, so populate the map null-tolerantly.
        final Map<Long, LovGetListDto> employeeLov = new HashMap<>();
        empPoids.forEach(p -> employeeLov.put(p, lovDataService.getDetailsByPoidAndLovName(p, EMPLOYEE_NAME)));

        final Map<Long, LovGetListDto> allowanceDeductionLov = new HashMap<>();
        alwdedPoids.forEach(p -> allowanceDeductionLov.put(p, lovDataService.getDetailsByPoidAndLovName(p, EMP_ALOW_DEDUCTION)));

        variables.forEach(row -> {
            Object empPoidValue = row.get(EMPLOYEE_POID);
            if (empPoidValue != null) {
                row.put("employeeLov", employeeLov.get(((Number) empPoidValue).longValue()));
            }
            Object alwdedPoidValue = row.get(ALLOWANCE_DEDUCTION_POID);
            if (alwdedPoidValue != null) {
                row.put("allowanceDeductionLov", allowanceDeductionLov.get(((Number) alwdedPoidValue).longValue()));
            }
        });

        return new VariableLoadResponse((String) result.get(P_STATUS), variables);
    }

    @Override
    public LoansAdvancesResponse loadLoansAdvances(Long transactionPoid, Long settlementPoid, Long empPoid, LocalDate payrollDate) {
        List<Map<String, Object>> loansAdvances = jdbcTemplate.execute((ConnectionCallback<List<Map<String, Object>>>) connection -> {
            try (CallableStatement cs = connection.prepareCall("{call PROC_HR_RECURRING_TO_PAYROLL(?,?,?,?,?)}")) {
                setNumeric(cs, 1, transactionPoid);
                setNumeric(cs, 2, settlementPoid);
                setNumeric(cs, 3, empPoid);
                setDate(cs, 4, payrollDate);
                cs.registerOutParameter(5, OracleTypes.CURSOR);
                cs.execute();
                return extractCursor(cs.getObject(5));
            }
        });
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                "Loans and Advances / Recurring Deductions refreshed...");
        
        Set<Long> empPoids = loansAdvances.stream()
                .map(map -> map.get(EMPLOYEE_POID))
                .filter(Objects::nonNull)
                .map(value -> ((Number) value).longValue())
                .collect(Collectors.toSet());

        // Collectors.toMap rejects null values; the LOV lookup may return null
        // for a poid with no master entry, so populate the map null-tolerantly.
        final Map<Long, LovGetListDto> employeeLov = new HashMap<>();
        empPoids.forEach(p -> employeeLov.put(p, lovDataService.getDetailsByPoidAndLovName(p, EMPLOYEE_NAME)));

        loansAdvances.forEach(row -> {
            Object poidValue = row.get(EMPLOYEE_POID);
            if (poidValue != null) {
                Long employeePoid = ((Number) poidValue).longValue();
                row.put("employeeLov", employeeLov.get(employeePoid));
            }
        });

        return new LoansAdvancesResponse(loansAdvances);
    }

    @Override
    public JvCreationResponse createJv(Long userPoid, Long transactionPoid, String bankCash) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));

        String bankCashParam = (bankCash != null && !bankCash.isEmpty()) ? bankCash : BANK;

        Map<String, Object> jvResult = execute(
                "PROC_HR_PAYROLL_CREATE_JV",
                List.of(
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_POID, Types.NUMERIC),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR),
                        new SqlParameter(P_BANK_CASH, Types.VARCHAR)
                ),
                params(
                        P_LOGIN_USER_POID, userPoid,
                        P_PAYROLL_POID, transactionPoid,
                        P_BANK_CASH, bankCashParam
                )
        );

        Map<String, Object> provJvResult = execute(
                "PROC_HR_PAYROLL_CREATE_PROV_JV",
                List.of(
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_POID, Types.NUMERIC),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(
                        P_LOGIN_USER_POID, userPoid,
                        P_PAYROLL_POID, transactionPoid
                )
        );

        Map<String, Object> bdvResult = execute(
                "PROC_HR_PAYROLL_CREATE_BDV",
                List.of(
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_POID, Types.NUMERIC),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(
                        P_LOGIN_USER_POID, userPoid,
                        P_PAYROLL_POID, transactionPoid
                )
        );

        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                "JV and Bank DV created...");
        loggingService.createLogDetailsEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                "PAYROLL_JV_STATUS", null, String.valueOf(jvResult.get(P_STATUS)),
                "Create JV", HR_PAYROLL_HDR);
        loggingService.createLogDetailsEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                "PROV_JV_STATUS", null, String.valueOf(provJvResult.get(P_STATUS)),
                "Create Provision JV", HR_PAYROLL_HDR);
        loggingService.createLogDetailsEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                "BANK_DV_STATUS", null, String.valueOf(bdvResult.get(P_STATUS)),
                "Create Bank DV", HR_PAYROLL_HDR);

        return new JvCreationResponse(
                String.valueOf(jvResult.get(P_STATUS)),
                String.valueOf(provJvResult.get(P_STATUS)),
                String.valueOf(bdvResult.get(P_STATUS))
        );
    }

    @Override
    public BankFileResponse generateBankFile(Long transactionPoid) {
        return executeBankFileGeneration(transactionPoid, FILE);
    }

    @Override
    public BankFileResponse hsbcApiTransfer(Long transactionPoid) {
        return executeBankFileGeneration(transactionPoid, API);
    }

    @Override
    public PayrollActionResponse syncHrData() {
        Map<String, Object> result = execute(
                "SYNC_HR_PRODUCTION_TO_PAYROLL",
                List.of(new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params()
        );
        String status = (String) result.get(P_STATUS);
        String logMsg = (status != null) ? "Sync HR Data: " + status : "Sync HR Data executed...";
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), null, logMsg);
        return new PayrollActionResponse(status, "HR Data sync completed");
    }

    @Override
    public PayrollActionResponse sendEmail(Long transactionPoid, PayrollActionRequest request) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));
        if (request.getScheduleOn() == null) {
            throw new ValidationException("Please enter a payslip schedule time.");
        }
        String resend = request.getResend() != null ? request.getResend() : N;
        Map<String, Object> result = execute(
                "PROC_PAYROLL_SEND_EMAIL",
                List.of(
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_POID, Types.NUMERIC),
                        new SqlParameter(P_RESEND, Types.VARCHAR),
                        new SqlParameter(P_PAYSLIP_SCHEDULE_TIME, Types.TIMESTAMP),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(
                        P_LOGIN_USER_POID, UserContext.getUserPoid(),
                        P_PAYROLL_POID, transactionPoid,
                        P_RESEND, resend,
                        P_PAYSLIP_SCHEDULE_TIME, request.getScheduleOn()
                )
        );
        logProcedureResult(transactionPoid, result,
                String.format("Payslip email scheduled on %s (resend=%s).", request.getScheduleOn(), resend),
                null);
        return new PayrollActionResponse((String) result.get(P_STATUS), "Email scheduling completed");
    }

    // ─── PRINT ───────────────────────────────────────────────────────────────

    @Override
    public byte[] printPayslip(Long transactionPoid) throws JRException {
        return generatePdf("Payroll/Payslip_without_email.jrxml", transactionPoid);
    }

    @Override
    public byte[] printPreview(Long transactionPoid) throws JRException {
        return generatePdf("Payroll/Payslip.jrxml", transactionPoid);
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

    private BankFileResponse executeBankFileGeneration(Long transactionPoid, String mode) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(PAYROLL_NOT_FOUND + transactionPoid));
        Map<String, Object> result = execute(
                "PROC_BANK_FILE_GENERATION",
                List.of(
                        new SqlParameter(P_COMPANY_POID, Types.NUMERIC),
                        new SqlParameter(P_TRNNO, Types.NUMERIC),
                        new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC),
                        new SqlOutParameter(P_FILE_NAME, Types.VARCHAR),
                        new SqlParameter(P_APIFILE, Types.VARCHAR)
                ),
                params(
                        P_COMPANY_POID, UserContext.getCompanyPoid(),
                        P_TRNNO, transactionPoid,
                        P_LOGIN_USER_POID, UserContext.getUserPoid(),
                        P_APIFILE, mode
                )
        );
        String logMsg = FILE.equals(mode) ? "Payroll bank file generated..." : "HSBC API transfer initiated...";
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logMsg);
        return new BankFileResponse((String) result.get(P_FILE_NAME), "Success");
    }

    /**
     * Writes a summary log after a stored-procedure call.
     * Skips on ERROR; uses successMsg on clean result, warningMsg (if set) on WARNING.
     */
    private void logProcedureResult(Long transactionPoid, Map<String, Object> result,
                                    String successMsg, String warningMsg) {
        String status = (String) result.get(P_STATUS);
        if (status != null && status.toUpperCase().contains(ERROR)) {
            return;
        }
        String msg = (status != null && status.toUpperCase().contains(WARNING) && warningMsg != null)
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
        
        // Validate employees are active and not terminated
//        validateEmployeeActiveStatus(
//                request.getAttendTranPoid(),
//                request.getPayrollMonth()
//        );

        validateWorkingDays(
                request.getAttendTranPoid()
        );


        callValidateProc(existingPoid, request.getAttendTranPoid(), payrollMonth, BEFORE_SAVE);
    }

    private void validateBeforeEdit(Long transactionPoid, HrPayrollHdr existing) {
        callValidateProc(transactionPoid, existing.getAttendTranPoid(), existing.getPayrollMonth(), BEFORE_EDIT);
    }

    private void callValidateProc(Long docPoid, Long attendTranPoid, LocalDate payrollDate, String actionType) {
        String status = runValidateProc(docPoid, attendTranPoid, payrollDate, actionType);
        if (status != null && status.toUpperCase().contains(ERROR)) {
            throw new ValidationException(status);
        }
    }

    /**
     * Runs PROC_HR_PAYROLL_VALIDATE and returns the raw P_STATUS message from the proc
     * without throwing, so callers (e.g. the GET response) can decide how to react.
     */
    private String runValidateProc(Long docPoid, Long attendTranPoid, LocalDate payrollDate, String actionType) {
        Map<String, Object> result = execute(
                "PROC_HR_PAYROLL_VALIDATE",
                List.of(
                        new SqlParameter(P_PAYROLL_POID, Types.NUMERIC),
                        new SqlParameter(P_ATTENDANCE_POID, Types.NUMERIC),
                        new SqlParameter(P_PAYROLL_DATE, Types.DATE),
                        new SqlParameter(P_VALIDATE_ACTION, Types.VARCHAR),
                        new SqlOutParameter(P_STATUS, Types.VARCHAR)
                ),
                params(
                        P_PAYROLL_POID, docPoid,
                        P_ATTENDANCE_POID, attendTranPoid,
                        P_PAYROLL_DATE, payrollDate,
                        P_VALIDATE_ACTION, actionType
                )
        );
        return (String) result.get(P_STATUS);
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
                ? request.getSuppressArrearsValidation() : N);
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
        
        // Validate recurring amounts before processing
        validateRecurringAmounts(requests);
        
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

    private void setNumeric(CallableStatement cs, int index, Long value) throws SQLException {
        if (value == null) {
            cs.setNull(index, Types.NUMERIC);
        } else {
            cs.setLong(index, value);
        }
    }

    private void setDate(CallableStatement cs, int index, LocalDate value) throws SQLException {
        if (value == null) {
            cs.setNull(index, Types.DATE);
        } else {
            cs.setDate(index, java.sql.Date.valueOf(value));
        }
    }

    private List<Map<String, Object>> extractCursor(Object cursor) throws SQLException {
        if (!(cursor instanceof ResultSet rs)) {
            return List.of();
        }
        try (ResultSet open = rs) {
            return new RowMapperResultSetExtractor<>(new ColumnMapRowMapper()).extractData(open);
        }
    }

    // ─── ADDITIONAL VALIDATIONS ──────────────────────────────────────────────

    /**
     * Validates that employees are active and not discontinued before payroll period end date
     */
    private void validateEmployeeActiveStatus(
            Long attendTranPoid,
            LocalDate payrollPeriodEndDate) {

        String sql =
                "SELECT DISTINCT " +
                        "       M." + EMPLOYEE_POID + ", " +
                        "       M.EMPLOYEE_NAME, " +
                        "       M.DISCONTINUED_DATE, " +
                        "       M.JOIN_DATE, " +
                        "       M.ACTIVE, " +
                        "       M.DISCONTINUED " +
                        "FROM HR_ATTENDANCE_MONTHLY_DTL D " +
                        "JOIN HR_EMPLOYEE_MASTER M " +
                        "     ON D." + EMPLOYEE_POID + " = M." + EMPLOYEE_POID + " " +
                        "WHERE D." + TRANSACTION_POID + " = ? " +
                        "AND (" +
                        "    NVL(M.ACTIVE, 'N') = 'N' " +
                        "    OR M.JOIN_DATE > ? " +
                        "    OR (NVL(M.DISCONTINUED, 'N') = 'Y' " +
                        "        AND M.DISCONTINUED_DATE IS NOT NULL " +
                        "        AND TRUNC(M.DISCONTINUED_DATE) < ?)" +
                        ")";

        List<Map<String, Object>> inactiveEmployees =
                jdbcTemplate.queryForList(
                        sql,
                        attendTranPoid,
                        java.sql.Date.valueOf(payrollPeriodEndDate),
                        java.sql.Date.valueOf(payrollPeriodEndDate)
                );

        if (!inactiveEmployees.isEmpty()) {

            // Separate employees by status for better organization
            List<String> inactiveStatusEmployees = new ArrayList<>();
            List<String> notJoinedEmployees = new ArrayList<>();
            List<String> discontinuedEmployees = new ArrayList<>();

            for (Map<String, Object> emp : inactiveEmployees) {
                String employeeId = String.valueOf(emp.get(EMPLOYEE_POID));
                String employeeName = String.valueOf(emp.get(EMPLOYEE_NAME));
                
                // Check if employee name looks like test data (all caps, very long, random characters)
                boolean isTestData = employeeName == null || employeeName.equals(NULL_STRING) || 
                                   employeeName.length() > 30 || 
                                   (employeeName.matches("[A-Z]{20,}") && !employeeName.contains(" "));
                
                String displayName = isTestData ? EMPLOYEE_ID_PREFIX + employeeId : employeeName;
                
                // Check the reason for inactivity
                String activeStatus = String.valueOf(emp.get(ACTIVE));
                String discontinuedStatus = String.valueOf(emp.get(DISCONTINUED));
                
                boolean isNotActive = N.equals(activeStatus) || NULL_STRING.equals(activeStatus);
                boolean isNotJoined = emp.get(JOIN_DATE) != null && 
                    ((java.sql.Timestamp) emp.get(JOIN_DATE)).toLocalDateTime().toLocalDate().isAfter(payrollPeriodEndDate);
                boolean isDiscontinued = Y.equals(discontinuedStatus) && emp.get(DISCONTINUED_DATE) != null;
                
                if (isNotActive && !isDiscontinued && !isNotJoined) {
                    // Employee is marked as inactive but not discontinued
                    inactiveStatusEmployees.add(displayName + (isTestData ? "" : ID_PREFIX + employeeId + ID_SUFFIX));
                } else if (isNotJoined) {
                    // Employee hasn't joined yet
                    notJoinedEmployees.add(displayName + (isTestData ? "" : ID_PREFIX + employeeId + ID_SUFFIX));
                } else if (isDiscontinued) {
                    // Employee is discontinued
                    String discontinuedDate = "Unknown";
                    if (emp.get(DISCONTINUED_DATE) != null) {
                        try {
                            java.sql.Timestamp timestamp = (java.sql.Timestamp) emp.get(DISCONTINUED_DATE);
                            discontinuedDate = timestamp.toLocalDateTime().toLocalDate().toString();
                        } catch (Exception e) {
                            discontinuedDate = emp.get(DISCONTINUED_DATE).toString().split(" ")[0];
                        }
                    }
                    discontinuedEmployees.add(displayName + 
                        (isTestData ? "" : ID_PREFIX + employeeId + ID_SUFFIX) + 
                        DISCONTINUED_SUFFIX + discontinuedDate);
                }
            }

            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("Payroll cannot be processed due to inactive employees:\n\n");
            
            if (!inactiveStatusEmployees.isEmpty()) {
                errorMsg.append("Inactive Employees (").append(inactiveStatusEmployees.size()).append("):\n");
                for (int i = 0; i < Math.min(inactiveStatusEmployees.size(), 10); i++) {
                    errorMsg.append("• ").append(inactiveStatusEmployees.get(i)).append("\n");
                }
                if (inactiveStatusEmployees.size() > 10) {
                    errorMsg.append(MORE_EMPLOYEES_MSG).append(inactiveStatusEmployees.size() - 10).append(MORE_EMPLOYEES_SUFFIX);
                }
                errorMsg.append("\n");
            }
            
            if (!discontinuedEmployees.isEmpty()) {
                errorMsg.append("Discontinued Employees (").append(discontinuedEmployees.size()).append("):\n");
                for (int i = 0; i < Math.min(discontinuedEmployees.size(), 10); i++) {
                    errorMsg.append("• ").append(discontinuedEmployees.get(i)).append("\n");
                }
                if (discontinuedEmployees.size() > 10) {
                    errorMsg.append(MORE_EMPLOYEES_MSG).append(discontinuedEmployees.size() - 10).append(MORE_EMPLOYEES_SUFFIX);
                }
                errorMsg.append("\n");
            }
            
            if (!notJoinedEmployees.isEmpty()) {
                errorMsg.append("Employees Not Yet Joined (").append(notJoinedEmployees.size()).append("):\n");
                for (int i = 0; i < Math.min(notJoinedEmployees.size(), 10); i++) {
                    errorMsg.append("• ").append(notJoinedEmployees.get(i)).append("\n");
                }
                if (notJoinedEmployees.size() > 10) {
                    errorMsg.append(MORE_EMPLOYEES_MSG).append(notJoinedEmployees.size() - 10).append(MORE_EMPLOYEES_SUFFIX);
                }
                errorMsg.append("\n");
            }
            
            errorMsg.append("Please remove these employees from the attendance data or update their status before processing payroll.");

            throw new ValidationException(errorMsg.toString().trim());
        }
    }
    

    /**
     * Validates that recurring amounts do not exceed balance amounts
     */
    private void validateRecurringAmounts(List<HrPayrollRecurringDtlRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        
        for (HrPayrollRecurringDtlRequest request : requests) {
            if (request.getRecurAmount() != null && request.getBalAmt() != null && 
                request.getRecurAmount().compareTo(request.getBalAmt()) > 0) {
                throw new ValidationException(
                        String.format("Recurring amount (%s) cannot exceed balance amount (%s) for employee ID: %s", 
                                request.getRecurAmount(), 
                                request.getBalAmt(), 
                                request.getEmployeePoid()));
            }
        }
    }

    /**
     * Validate employee working days should not exceed payroll period working days
     */
    private void validateWorkingDays(Long attendTranPoid) {

        log.info("Starting working days validation for TRANSACTION_POID: {}", attendTranPoid);

        String hdrSql =
                "SELECT " + ATTENDANCE_FROM + ", " + ATTENDANCE_TO + " " +
                        "FROM HR_ATTENDANCE_MONTHLY_HDR " +
                        "WHERE " + TRANSACTION_POID + " = ?";

        Map<String, Object> hdr;

        try {
            hdr = jdbcTemplate.queryForMap(hdrSql, attendTranPoid);
        } catch (Exception e) {
            log.error("HDR not found for TRANSACTION_POID: {}", attendTranPoid);
            throw new ValidationException(
                    "Attendance monthly header not found for transaction: " + attendTranPoid
            );
        }

        LocalDate fromDate =
                ((java.sql.Timestamp) hdr.get(ATTENDANCE_FROM))
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

        LocalDate toDate =
                ((java.sql.Timestamp) hdr.get(ATTENDANCE_TO))
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

        long payrollWorkingDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;

        log.info("Payroll Period: {} to {} => Payroll Working Days: {}",
                fromDate, toDate, payrollWorkingDays);

        String employeeSql =
                "SELECT " + EMPLOYEE_POID + ", " + MONTHLY_WOKING_DAYS + " " +
                        "FROM HR_ATTENDANCE_MONTHLY_DTL " +
                        "WHERE " + TRANSACTION_POID + " = ?";

        List<Map<String, Object>> employeeDays =
                jdbcTemplate.queryForList(employeeSql, attendTranPoid);

        log.info("Total employees found for validation: {}", employeeDays.size());

        List<String> invalidEmployees = new ArrayList<>();

        for (Map<String, Object> row : employeeDays) {

            Long employeePoid =
                    row.get(EMPLOYEE_POID) != null
                            ? ((Number) row.get(EMPLOYEE_POID)).longValue()
                            : null;

            Object rawDays = row.get(MONTHLY_WOKING_DAYS);

            BigDecimal empWorkingDays =
                    rawDays != null
                            ? new BigDecimal(rawDays.toString())
                            : BigDecimal.ZERO;

            log.debug("Employee ID: {} | Monthly Working Days: {} | Payroll Days: {}",
                    employeePoid, empWorkingDays, payrollWorkingDays);

            if (empWorkingDays.longValue() > payrollWorkingDays) {

                log.warn("VALIDATION FAILED -> Employee ID: {} exceeds payroll days ({} > {})",
                        employeePoid, empWorkingDays, payrollWorkingDays);

                invalidEmployees.add(
                        EMPLOYEE_ID_PREFIX + employeePoid +
                                " (Employee Working Days: " + empWorkingDays +
                                ", Payroll Working Days: " + payrollWorkingDays + ")"
                );
            }
        }

        if (!invalidEmployees.isEmpty()) {

            log.error("Working days validation FAILED. Invalid employees count: {}",
                    invalidEmployees.size());

            throw new ValidationException(
                    "Working days must not exceed payroll period working days. " +
                            invalidEmployees
            );
        }

        log.info("Working days validation SUCCESS for TRANSACTION_POID: {}", attendTranPoid);
    }
    
    // ─── DTO MAPPING HELPERS ─────────────────────────────────────────────────
    
    private List<HrPayrollDtlResponse> mapToPayrollDtlResponse(List<HrPayrollDtl> entities) {
        return entities.stream().map(entity -> {
            HrPayrollDtlResponse dto = new HrPayrollDtlResponse();
            dto.setDetRowId(entity.getDetRowId());
            dto.setEmployeePoid(entity.getEmployeePoid());
            dto.setWorkedDays(entity.getWorkedDays());
            dto.setBasicSalary(entity.getBasicSalary());
            dto.setBasicSalaryPayable(entity.getBasicSalaryPayable());
            dto.setFixedAllowance(entity.getFixedAllowance());
            dto.setFixedOt(entity.getFixedOt());
            dto.setTransportAllowance(entity.getTransportAllowance());
            dto.setHraAllowance(entity.getHraAllowance());
            dto.setGrossSalary(entity.getGrossSalary());
            dto.setLoanDeduction(entity.getLoanDeduction());
            dto.setGosiDeduction(entity.getGosiDeduction());
            dto.setTotDeductions(entity.getTotDeductions());
            dto.setNetSalary(entity.getNetSalary());
            dto.setAccountNo(entity.getAccountNo());
            dto.setHoldSalary(entity.getHoldSalary());
            dto.setHoldReason(entity.getHoldReason());
            dto.setRemarks(entity.getRemarks());
            return dto;
        }).toList();
    }
    
    private List<HrPayrollVarAlwdedDtlResponse> mapToVarAlwdedDtlResponse(List<HrPayrollVarAlwdedDtl> entities) {
        return entities.stream().map(entity -> {
            HrPayrollVarAlwdedDtlResponse dto = new HrPayrollVarAlwdedDtlResponse();
            dto.setDetRowId(entity.getDetRowId());
            dto.setEmployeePoid(entity.getEmployeePoid());
            if (entity.getEmployeePoid() != null) {
                dto.setEmployeeLov(lovDataService.getDetailsByPoidAndLovName(
                        entity.getEmployeePoid(), EMPLOYEE_NAME));
            }
            dto.setAllowanceDeductionPoid(entity.getAllowanceDeductionPoid());
            dto.setAmount(entity.getAmount());
            dto.setRemarks(entity.getRemarks());
            return dto;
        }).toList();
    }
    
    private List<HrPayrollProvisionDtlResponse> mapToProvisionDtlResponse(List<HrPayrollProvisionDtl> entities) {
        return entities.stream().map(entity -> {
            HrPayrollProvisionDtlResponse dto = new HrPayrollProvisionDtlResponse();
            dto.setDetRowId(entity.getDetRowId());
            dto.setEmployeePoid(entity.getEmployeePoid());
            dto.setBasicSalary(entity.getBasicSalary());
            dto.setBasicSalaryPayable(entity.getBasicSalaryPayable());
            dto.setWorkedDays(entity.getWorkedDays());
            dto.setLeaveSalary(entity.getLeaveSalary());
            dto.setAirPassage(entity.getAirPassage());
            dto.setIndemnity(entity.getIndemnity());
            dto.setGosi(entity.getGosi());
            dto.setLmra(entity.getLmra());
            dto.setTotalProvision(entity.getTotalProvision());
            dto.setRemarks(entity.getRemarks());
            return dto;
        }).toList();
    }
    
    private List<HrPayrollRecurringDtlResponse> mapToRecurringDtlResponse(List<HrPayrollRecurringDtl> entities) {
        return entities.stream().map(entity -> {
            HrPayrollRecurringDtlResponse dto = new HrPayrollRecurringDtlResponse();
            dto.setDetRowId(entity.getDetRowId());
            dto.setEmployeePoid(entity.getEmployeePoid());
            dto.setRefNo(entity.getRefNo());
            dto.setBalAmt(entity.getBalAmt());
            dto.setRecurAmount(entity.getRecurAmount());
            dto.setRemarks(entity.getRemarks());
            return dto;
        }).toList();
    }
}
