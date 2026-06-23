package com.asg.payroll.employeeappraisal.service;
import com.asg.common.lib.dto.*;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.service.PrintService;
import com.asg.common.lib.utility.PaginationUtil;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Lazy;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;

import javax.sql.DataSource;

import com.asg.payroll.employeeappraisal.dto.HrAppraisalActionRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalDtlRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalDtlResponse;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRecalculationRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRequest;
import com.asg.payroll.employeeappraisal.entity.HrAppraisalDtl;
import com.asg.payroll.employeeappraisal.entity.HrAppraisalDtlId;
import com.asg.payroll.employeeappraisal.entity.HrAppraisalHdr;
import com.asg.payroll.employeeappraisal.enums.ActionType;
import com.asg.payroll.employeeappraisal.repository.HrAppraisalDtlRepository;
import com.asg.payroll.employeeappraisal.repository.HrAppraisalHdrRepository;
import com.asg.payroll.salarydetails.repository.HrEmployeeSalaryMasterRepository;
import com.asg.payroll.employeeappraisal.repository.HrPayrollVarAlwdedDtlRepository;
import com.asg.payroll.exceptions.ResourceNotFoundException;
import com.asg.payroll.exceptions.ValidationException;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.EmptyResultDataAccessException;
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
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@Transactional
public class HrAppraisalServiceImpl implements HrAppraisalService {


    private final EntityManager entityManager;

    public HrAppraisalServiceImpl(EntityManager entityManager, @Lazy HrAppraisalService self, HrAppraisalHdrRepository hdrRepository, HrAppraisalDtlRepository dtlRepository, HrEmployeeSalaryMasterRepository salaryMasterRepository, HrPayrollVarAlwdedDtlRepository payrollVarDtlRepository, DocumentSearchService documentSearchService, DocumentDeleteService documentDeleteService, LoggingService loggingService, PrintService printService, DataSource dataSource, JdbcTemplate jdbcTemplate, LovDataService lovDataService) {
        this.entityManager = entityManager;
        this.self = self;
        this.hdrRepository = hdrRepository;
        this.dtlRepository = dtlRepository;
        this.salaryMasterRepository = salaryMasterRepository;
        this.payrollVarDtlRepository = payrollVarDtlRepository;
        this.documentSearchService = documentSearchService;
        this.documentDeleteService = documentDeleteService;
        this.loggingService = loggingService;
        this.printService = printService;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.lovDataService = lovDataService;
    }
    private final HrAppraisalService self;
    private final HrAppraisalHdrRepository hdrRepository;
    private final HrAppraisalDtlRepository dtlRepository;
    private final HrEmployeeSalaryMasterRepository salaryMasterRepository;
    private final HrPayrollVarAlwdedDtlRepository payrollVarDtlRepository;
    private final DocumentSearchService documentSearchService;
    private final DocumentDeleteService documentDeleteService;
    private final LoggingService loggingService;
    private final PrintService printService;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final LovDataService lovDataService;


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
    private static final String BASE_DETAIL_SQL =
            "SELECT d.TRANSACTION_POID, d.DET_ROW_ID, d.EMPLOYEE_POID, d.DESIGNATION_POID, d.JOIN_DATE, " +
            "d.CUR_AIR_ENTITLE, d.CUR_BONUS, d.CUR_INCREMENT, d.CUR_BASIC_SALARY, d.CUR_FA_ALW, " +
            "d.CUR_TA_ALW, d.CUR_HRA_ALW, d.CUR_FIXOT_ALW, d.CUR_SPL_ALW, d.CUR_OTH_ALW, d.CUR_AVGOT, d.CUR_GROSS_PAY, " +
            "d.NEW_AIR_ENTITLE, d.NEW_BONUS, d.NEW_INCREMENT_PER, d.NEW_BASIC_SALARY, d.NEW_FA_ALW, " +
            "d.NEW_TA_ALW, d.NEW_HRA_ALW, d.NEW_FIXOT_ALW, d.NEW_SPL_ALW, d.NEW_OTH_ALW, d.NEW_AVGOT, d.NEW_GROSS_PAY, " +
            "d.STATUS, d.NET_INCREMENT, d.LAST_INCREMENT_DATE, d.LAST_INCREMENT_AMT, d.LAST_BONUS, " +
            "d.CUR_TICKET_PERIOD, d.CUR_NO_OF_TICKETS, d.NEW_TICKET_PERIOD, d.NEW_NO_OF_TICKETS, " +
            "d.NEW_DESIGNATION_POID, d.ARREARS, d.NEW_BONUS_PER, d.LETTER_EMAILED_ON, d.GRID_LISTING_METHOD, " +
            "d.REGISTERED_SALARY, d.CUR_MONTHLY_CTC, d.CUR_YEARLY_CTC, d.NEW_MONTHLY_CTC, d.NEW_YEARLY_CTC, " +
            "d.LAST_DESIGNATION_POID, d.LAST_PROMOTION_DATE, d.CREATED_BY, d.CREATED_DATE, d.LASTMODIFIED_BY, d.LASTMODIFIED_DATE, " +
            "e.EMPLOYEE_CODE, e.EMPLOYEE_NAME, e.EMPLOYEE_NAME2 " +
            "FROM HR_APPRAISAL_DTL d " +
            "LEFT JOIN HR_EMPLOYEE_MASTER e ON d.EMPLOYEE_POID = e.EMPLOYEE_POID " +
            "WHERE d.TRANSACTION_POID = ?";

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
        List<HrAppraisalDtlResponse> details = fetchDetailsWithEmployeeData(transactionPoid, null, null, null, null);
        Map<String, Object> out = new HashMap<>();
        out.put("header", hdr);
        out.put("details", details);
        out.put("totals", computeTotals(details));
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFilteredDetails(Long transactionPoid, Long departmentPoid, Long designationPoid, String listingMethod, String employeeName) {
        List<HrAppraisalDtlResponse> details = fetchDetailsWithEmployeeData(transactionPoid, departmentPoid, designationPoid, listingMethod, employeeName);
        Map<String, Object> out = new HashMap<>();
        out.put("details", details);
        out.put("totals", computeTotals(details));
        out.put("totalRecords", details.size());
        return out;
    }

    @Override
    public Map<String, Object> createAppraisal(HrAppraisalRequest request) {
        validateHeaderForLegacySave(request);
        if (request.getTransactionDate() != null) {
            validateFinancialYear(request.getTransactionDate(), null);
        }
        HrAppraisalHdr hdr = new HrAppraisalHdr();
        mapHeader(request, hdr);
        hdr.setGroupPoid(UserContext.getGroupPoid());
        hdr.setCompanyPoid(UserContext.getCompanyPoid());
        hdr.setDeleted("N");
        HrAppraisalHdr saved = hdrRepository.saveAndFlush(hdr);
        upsertDetails(saved.getTransactionPoid(), request.getDetails(), true);
        entityManager.refresh(saved);
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), saved.getTransactionPoid().toString(),String.format("%s %s", LogDetailsEnum.CREATED.getDescription(), saved.getDocRef()));
        return self.getAppraisalById(saved.getTransactionPoid());
    }

    @Override
    public Map<String, Object> updateAppraisal(Long transactionPoid, HrAppraisalRequest request) {
        validateHeaderForLegacySave(request);
        HrAppraisalHdr existing = hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(APPRAISAL_NOT_FOUND + transactionPoid));
        if (request.getTransactionDate() != null) {
            validateFinancialYear(request.getTransactionDate(), existing.getTransactionDate());
        }
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
        validateFinancialYear(hdr.getTransactionDate(), null);

        documentDeleteService.deleteDocument(
                transactionPoid,
                "HR_APPRAISAL_HDR",
                TRANSACTION_POID,
                deleteReasonDto,
                hdr.getTransactionDate()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDetailsSp(Long transactionPoid, Long employeePoid) {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall("BEGIN PROC_HR_APPRAISAL_GET_DETAILS(?,?,?,?,?); END;")) {

            cs.setObject(1, UserContext.getCompanyPoid(), Types.NUMERIC);
            cs.setObject(2, transactionPoid, Types.NUMERIC);
            cs.setObject(3, employeePoid, Types.NUMERIC);
            cs.registerOutParameter(4, OracleTypes.CURSOR);
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.execute();

            result.put("status", cs.getString(5));

            try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                if (rs != null && rs.next()) {
                    result.put("serviceYears", rs.getBigDecimal("SERVICE_YEARS"));
                    result.put("empCode",       rs.getString("EMP_CODE"));
                    result.put("empName",        rs.getString("EMP_NAME"));
                    String salesmanName = rs.getString("SALESMAN_NAME");
                    result.put("salesmanName",   salesmanName);
                    result.put("showSalesmanRpt", salesmanName != null && !salesmanName.isBlank());
                }
            }
        } catch (SQLException e) {
            log.warn("PROC_HR_APPRAISAL_GET_DETAILS not available: {}", e.getMessage());
        }

        result.put("detail", fetchDetailByEmployeePoid(transactionPoid, employeePoid));
        return result;
    }

    @Override
    public Map<String, Object> loadAppraisalDataSp(Long transactionPoid, String actionType) {
        if (actionType == null || actionType.isBlank()) {
            throw new ValidationException("Appraisal load action type is required (legacy: LOAD_EMPLOYEES_BLANK_DATA or LOAD_EMPLOYEES_WITH_CURRENT_DATA).");
        }
        Map<String, Object> result = execute(
                "PROC_HR_LOAD_APPRAISAL_DATA",
                List.of(new SqlParameter(P_TRANS_POID, Types.NUMERIC), new SqlParameter(P_LOGIN_USER, Types.VARCHAR), new SqlParameter(P_LOGIN_COMPANY_POID, Types.NUMERIC), new SqlParameter("P_ACTION_TYPE", Types.VARCHAR), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_TRANS_POID, transactionPoid, P_LOGIN_USER, UserContext.getUserId(), P_LOGIN_COMPANY_POID, UserContext.getCompanyPoid(), "P_ACTION_TYPE", actionType)
        );
        appendRefreshedData(result, transactionPoid);
        return result;
    }

    @Override
    public Map<String, Object> clearAppraisalDataSp(Long transactionPoid) {
        Map<String, Object> result = execute(
                "PROC_HR_CLEAR_APPRAISAL_DATA",
                List.of(new SqlParameter(P_TRANS_POID, Types.NUMERIC), new SqlParameter(P_LOGIN_USER, Types.VARCHAR), new SqlParameter(P_LOGIN_COMPANY_POID, Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_TRANS_POID, transactionPoid, P_LOGIN_USER, UserContext.getUserId(), P_LOGIN_COMPANY_POID, UserContext.getCompanyPoid())
        );
        appendRefreshedData(result, transactionPoid);
        return result;
    }

    @Override
    public Map<String, Object> batchUpdateSp(Long transactionPoid, HrAppraisalActionRequest request) {
        BigDecimal basic = nz(request.getBasicIncrementPercent());
        BigDecimal bonus = nz(request.getBonusPercent());
        if (basic.compareTo(BigDecimal.ZERO) == 0 && bonus.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException("Both Basic and Bonus Percentages are zero...");
        }
        Map<String, Object> result = execute(
                "PROC_HR_APPRAISAL_BATCH_UPDATE",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlParameter(P_BASIC_INCREMENT_PERCENT, Types.NUMERIC), new SqlParameter(P_BONUS_PERCENT, Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid, P_BASIC_INCREMENT_PERCENT, request.getBasicIncrementPercent(), P_BONUS_PERCENT, request.getBonusPercent())
        );
        appendRefreshedData(result, transactionPoid);
        return result;
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
                if (request.getNetDiffAmount() == null) {
                    throw new ValidationException("Net difference amount is required for NET_DIFF_EDIT mode.");
                }
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
                BigDecimal netDiffAmt = nz(request.getNetDiffAmount());
                if (netDiffAmt.compareTo(BigDecimal.ZERO) > 0) {
                    HrAppraisalLegacyRecalculation.applyFromNetDifference(row, netDiffAmt, basicPercent, request.getPeriodFrom(), today);
                }
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
        if (employeePoid == null) {
            throw new ValidationException("No Employee record is selected.");
        }
        List<HrAppraisalDtl> dtls = dtlRepository.findByTransactionPoidAndEmployeePoid(transactionPoid, employeePoid);
        if (dtls.isEmpty()) {
            throw new ResourceNotFoundException("Employee not found in appraisal.");
        }
        // Match legacy: ADF committed STATUS='Updated' before calling PROC_HR_APPRAISAL_UPDATE_DATA
        dtls.forEach(dtl -> dtl.setStatus("Updated"));
        dtlRepository.saveAll(dtls);
        Map<String, Object> result = execute(
                "PROC_HR_APPRAISAL_UPDATE_DATA",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC), new SqlParameter("P_EMPLOYEE_POID", Types.NUMERIC), new SqlParameter("P_ADDITIONAL_DATA", Types.VARCHAR), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid, P_LOGIN_USER_POID, UserContext.getUserPoid(), "P_EMPLOYEE_POID", employeePoid, "P_ADDITIONAL_DATA", request.getAdditionalData())
        );
        appendRefreshedData(result, transactionPoid);
        return result;
    }

    @Override
    public Map<String, Object> updateMasterSp(Long transactionPoid, HrAppraisalActionRequest request) {
        HrAppraisalHdr hdr = hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(APPRAISAL_NOT_FOUND + transactionPoid));
        if (hdr.getApprovedBy() == null || hdr.getApprovedBy().isBlank()) {
            throw new ValidationException("Please enter approval by details... ");
        }

        List<Long> employeesWithoutSalary = dtlRepository.findByTransactionPoid(transactionPoid).stream()
                .filter(dtl -> dtl.getNetIncrement() != null && dtl.getNetIncrement().compareTo(BigDecimal.ZERO) != 0)
                .map(HrAppraisalDtl::getEmployeePoid)
                .filter(empPoid -> !salaryMasterRepository.existsByEmployeePoid(empPoid))
                .toList();

        if (!employeesWithoutSalary.isEmpty()) {
            throw new ValidationException("Some employees in this appraisal do not have salary records.");
        }
        return execute(
                "PROC_HR_APPRAISAL_UPDATE_MAST",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC), new SqlParameter(P_BASIC_INCREMENT_PERCENT, Types.NUMERIC), new SqlParameter(P_BONUS_PERCENT, Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid, P_LOGIN_USER_POID, UserContext.getUserPoid(), P_BASIC_INCREMENT_PERCENT, request.getBasicIncrementPercent(), P_BONUS_PERCENT, request.getBonusPercent())
        );
    }

    @Override
    public Map<String, Object> createJvSp(Long transactionPoid) {

        boolean hasBonusAmount = dtlRepository.findByTransactionPoid(transactionPoid).stream()
                .anyMatch(dtl -> dtl.getNewBonus() != null && dtl.getNewBonus().compareTo(BigDecimal.ZERO) > 0);
        if (!hasBonusAmount) {
            throw new ValidationException("No bonus amounts found in appraisal details. Cannot create JV.");
        }

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

        boolean hasBonusEmployees = dtlRepository.findByTransactionPoid(transactionPoid).stream()
                .anyMatch(dtl -> dtl.getNewBonus() != null && dtl.getNewBonus().compareTo(BigDecimal.ZERO) > 0);

        if (!hasBonusEmployees) {
            throw new ValidationException("No employees with bonus amount found for bank file generation.");
        }
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
        long arrearsCount = dtlRepository.findByTransactionPoid(transactionPoid).stream()
                .filter(dtl -> dtl.getArrears() != null && dtl.getArrears().compareTo(BigDecimal.ZERO) != 0)
                .count();
        long maxExistingRowId = payrollVarDtlRepository.findMaxDetRowIdByTransactionPoid(payrollPoid);
        if (maxExistingRowId > 0 && arrearsCount > 0) {
            throw new ValidationException("Payroll already has variable allowance entries. Please remove existing arrears entries before re-adding.");
        }
        return execute(
                "PROC_HR_APPRAISAL_ARREARS",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlParameter(P_LOGIN_USER_POID, Types.NUMERIC), new SqlParameter("P_PAYROLL_POID", Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid, P_LOGIN_USER_POID, UserContext.getUserPoid(), "P_PAYROLL_POID", payrollPoid)
        );
    }

    @Override
    public Map<String, Object> arrearsCalcSp(Long transactionPoid) {
        HrAppraisalHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(APPRAISAL_NOT_FOUND + transactionPoid));

        if (hdr.getPeriodFrom() == null) {
            throw new ValidationException("Appraisal effective date (period from) is not set. Cannot calculate arrears.");
        }

        boolean hasEligibleEmployees = dtlRepository.findByTransactionPoid(transactionPoid).stream()
                .anyMatch(dtl -> dtl.getNetIncrement() != null && dtl.getNetIncrement().compareTo(BigDecimal.ZERO) > 0);
        if (!hasEligibleEmployees) {
            throw new ValidationException("No employees with a positive net increment found. Arrears calculation requires at least one employee with an increment.");
        }

        Map<String, Object> result = execute(
                "PROC_HR_APPRAISAL_ARREARS_CALC",
                List.of(new SqlParameter(P_COMPANYID, Types.NUMERIC), new SqlParameter(P_TRANSACTION_POID, Types.NUMERIC), new SqlOutParameter(P_STATUS, Types.VARCHAR)),
                params(P_COMPANYID, UserContext.getCompanyPoid(), P_TRANSACTION_POID, transactionPoid)
        );
        appendRefreshedData(result, transactionPoid);
        return result;
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
        // employeePoid null → print letters for all employees (legacy PrintLetterForEmployees)
        // employeePoid set  → print single employee letter (legacy PrintLetterForSingleEmployee)
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
        Map<String, Object> result = new HashMap<>(call.execute(inParams));
        parameters.stream()
                .filter(p -> p instanceof SqlOutParameter)
                .map(p -> result.get(p.getName()))
                .filter(v -> v instanceof String)
                .map(v -> (String) v)
                .filter(s -> s.toUpperCase(Locale.ROOT).startsWith("ERROR"))
                .findFirst()
                .ifPresent(s -> { throw new ValidationException(s); });
        return result;
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
            switch (action) {
                case isCreated -> createDetail(transactionPoid, request, hdr, today);
                case isDeleted -> deleteDetail(transactionPoid, request);
                default -> updateDetail(transactionPoid, request, hdr, today);

            }
        }
    }

    private void createDetail(Long transactionPoid, HrAppraisalDtlRequest request, HrAppraisalHdr hdr, LocalDate today) {
        if (request.getEmployeePoid() != null && !dtlRepository.findByTransactionPoidAndEmployeePoid(transactionPoid, request.getEmployeePoid()).isEmpty()) {
            throw new ValidationException("Employee already exists in this appraisal.");
        }
        validateActiveEmployee(request.getEmployeePoid());
        validateEffectiveDateAfterLastIncrement(hdr.getPeriodFrom(), request.getLastIncrementDate(), request.getEmployeePoid());
        validateProposedSalaryNotLessThanCurrent(request);
        HrAppraisalDtl entity = new HrAppraisalDtl();
        entity.setTransactionPoid(transactionPoid);
        entity.setDetRowId(dtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1);
        entity.setDeleted("N");
        mapDetail(request, entity);
        HrAppraisalLegacyRecalculation.applyRecalculateGross(entity, hdr.getPeriodFrom(), today);
        HrAppraisalDtl saved = dtlRepository.save(entity);
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), String.valueOf(transactionPoid),
                String.format("Row Created on [Employee Appraisal Details] with DetRowId: %s", saved.getDetRowId()));
    }

    private void deleteDetail(Long transactionPoid, HrAppraisalDtlRequest request) {
        HrAppraisalDtlId id = buildDtlId(transactionPoid, request.getDetRowId());
        HrAppraisalDtl detail = dtlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Details", "DET_ROW_ID", id.getDetRowId()));
        dtlRepository.deleteById(id);
        loggingService.logDelete(detail, UserContext.getDocumentId(), id.getTransactionPoid().toString());
    }

    private void updateDetail(Long transactionPoid, HrAppraisalDtlRequest request, HrAppraisalHdr hdr, LocalDate today) {
        HrAppraisalDtlId id = buildDtlId(transactionPoid, request.getDetRowId());
        HrAppraisalDtl existing = dtlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appraisal detail not found for detRowId: " + request.getDetRowId()));
        validateEffectiveDateAfterLastIncrement(hdr.getPeriodFrom(), request.getLastIncrementDate(), request.getEmployeePoid());
        validateProposedSalaryNotLessThanCurrent(request);
        HrAppraisalDtl oldEntity = new HrAppraisalDtl();
        BeanUtils.copyProperties(existing, oldEntity);
        mapDetail(request, existing);
        HrAppraisalLegacyRecalculation.applyRecalculateGross(existing, hdr.getPeriodFrom(), today);
        HrAppraisalDtl saved = dtlRepository.save(existing);
        loggingService.createLog(oldEntity, existing, HrAppraisalDtl.class, UserContext.getDocumentId(), transactionPoid.toString(),
                String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", saved.getTransactionPoid(), saved.getDetRowId()));
    }

    private HrAppraisalDtlId buildDtlId(Long transactionPoid, Long detRowId) {
        HrAppraisalDtlId id = new HrAppraisalDtlId();
        id.setTransactionPoid(transactionPoid);
        id.setDetRowId(detRowId);
        return id;
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

    private void validateActiveEmployee(Long employeePoid) {
        if (employeePoid == null) return;
        try {
            String active = jdbcTemplate.queryForObject(
                    "SELECT ACTIVE FROM HR_EMPLOYEE_MASTER WHERE EMPLOYEE_POID = ?",
                    String.class, employeePoid);
            if (!"Y".equalsIgnoreCase(active)) {
                throw new ValidationException("Only active employees can be added to an appraisal. Employee ID: " + employeePoid);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeePoid);
        }
    }

    private void validateEffectiveDateAfterLastIncrement(LocalDate periodFrom, LocalDate lastIncrementDate, Long employeePoid) {
        if (periodFrom == null || lastIncrementDate == null) return;
        if (!periodFrom.isAfter(lastIncrementDate)) {
            throw new ValidationException(String.format(
                    "Appraisal effective date (%s) must be after the last increment date (%s)%s.",
                    periodFrom, lastIncrementDate,
                    employeePoid != null ? " for employee ID: " + employeePoid : ""));
        }
    }

    private void validateProposedSalaryNotLessThanCurrent(HrAppraisalDtlRequest request) {
        BigDecimal curBasic = nz(request.getCurBasicSalary());
        BigDecimal newBasic = nz(request.getNewBasicSalary());
        if (curBasic.compareTo(BigDecimal.ZERO) > 0 && newBasic.compareTo(BigDecimal.ZERO) > 0
                && newBasic.compareTo(curBasic) < 0) {
            throw new ValidationException("Proposed basic salary must be >= current basic salary.");
        }
        BigDecimal curGross = nz(request.getCurGrossPay());
        BigDecimal newGross = nz(request.getNewGrossPay());
        if (curGross.compareTo(BigDecimal.ZERO) > 0 && newGross.compareTo(BigDecimal.ZERO) > 0
                && newGross.compareTo(curGross) < 0) {
            throw new ValidationException("Proposed gross salary must be >= current gross salary.");
        }
    }

    private HrAppraisalDtlResponse fetchDetailByEmployeePoid(Long transactionPoid, Long employeePoid) {
        List<HrAppraisalDtlResponse> rows = jdbcTemplate.query(
                BASE_DETAIL_SQL + " AND d.EMPLOYEE_POID = ? AND (d.DELETED IS NULL OR d.DELETED = 'N') ORDER BY d.DET_ROW_ID",
                this::mapDtlRow, transactionPoid, employeePoid);
        if (rows.isEmpty()) return null;
        HrAppraisalDtlResponse row = rows.get(0);
        row.setEmployeeDet(lovDataService.getDetailsByPoidAndLovNameFast(employeePoid, "EMPLOYEE_NAME_WITH_SHORT"));
        List<Long> desPoids = Stream.of(row.getDesignationPoid(), row.getNewDesignationPoid(), row.getLastDesignationPoid())
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (!desPoids.isEmpty()) {
            Map<Long, LovGetListDto> desMap = lovDataService.getDetailsByPoidsAndLovName(desPoids, "DESIGNATION");
            row.setDesignationDet(desMap.get(row.getDesignationPoid()));
            row.setNewDesignationDet(desMap.get(row.getNewDesignationPoid()));
            row.setLastDesignationDet(desMap.get(row.getLastDesignationPoid()));
        }
        return row;
    }

    private List<HrAppraisalDtlResponse> fetchDetailsWithEmployeeData(Long transactionPoid, Long departmentPoid, Long designationPoid, String listingMethod, String employeeName) {
        StringBuilder sql = new StringBuilder(BASE_DETAIL_SQL + " AND (d.DELETED IS NULL OR d.DELETED = 'N')");

        List<Object> params = new ArrayList<>();
        params.add(transactionPoid);

        if (departmentPoid != null) {
            sql.append(" AND e.DEPARTMENT_POID = ?");
            params.add(departmentPoid);
        }
        if (designationPoid != null) {
            sql.append(" AND d.DESIGNATION_POID = ?");
            params.add(designationPoid);
        }
        if (listingMethod != null && !listingMethod.isBlank()) {
            if ("UPDATED".equalsIgnoreCase(listingMethod)) {
                sql.append(" AND d.STATUS = 'Updated'");
            } else if ("PENDING".equalsIgnoreCase(listingMethod)) {
                sql.append(" AND (d.STATUS IS NULL OR d.STATUS != 'Updated')");
            }
        }
        if (employeeName != null && !employeeName.isBlank()) {
            sql.append(" AND UPPER(e.EMPLOYEE_NAME) LIKE UPPER(?)");
            params.add("%" + employeeName.trim() + "%");
        }
        sql.append(" ORDER BY d.DET_ROW_ID");

        List<HrAppraisalDtlResponse> details = jdbcTemplate.query(sql.toString(), this::mapDtlRow, params.toArray());
        List<Long> empPoids = details.stream()
                .map(HrAppraisalDtlResponse::getEmployeePoid)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, LovGetListDto> empLovMap = lovDataService.getDetailsByPoidsAndLovName(empPoids, "EMPLOYEE_NAME_WITH_SHORT");
        details.forEach(d -> {
            if (d.getEmployeePoid() != null) d.setEmployeeDet(empLovMap.get(d.getEmployeePoid()));
        });
        return details;
    }

    private HrAppraisalDtlResponse mapDtlRow(ResultSet rs, int rowNum) throws SQLException {
        HrAppraisalDtlResponse r = new HrAppraisalDtlResponse();
        r.setTransactionPoid(getLong(rs, "TRANSACTION_POID"));
        r.setDetRowId(getLong(rs, "DET_ROW_ID"));
        r.setEmployeePoid(getLong(rs, "EMPLOYEE_POID"));
        r.setDesignationPoid(getLong(rs, "DESIGNATION_POID"));
        r.setJoinDate(toLocalDate(rs, "JOIN_DATE"));
        r.setCurAirEntitle(rs.getString("CUR_AIR_ENTITLE"));
        r.setCurBonus(getBd(rs, "CUR_BONUS"));
        r.setCurIncrement(getBd(rs, "CUR_INCREMENT"));
        r.setCurBasicSalary(getBd(rs, "CUR_BASIC_SALARY"));
        r.setCurFaAlw(getBd(rs, "CUR_FA_ALW"));
        r.setCurTaAlw(getBd(rs, "CUR_TA_ALW"));
        r.setCurHraAlw(getBd(rs, "CUR_HRA_ALW"));
        r.setCurFixotAlw(getBd(rs, "CUR_FIXOT_ALW"));
        r.setCurSplAlw(getBd(rs, "CUR_SPL_ALW"));
        r.setCurOthAlw(getBd(rs, "CUR_OTH_ALW"));
        r.setCurAvgot(getBd(rs, "CUR_AVGOT"));
        r.setCurGrossPay(getBd(rs, "CUR_GROSS_PAY"));
        r.setNewAirEntitle(rs.getString("NEW_AIR_ENTITLE"));
        r.setNewBonus(getBd(rs, "NEW_BONUS"));
        r.setNewIncrementPer(getBd(rs, "NEW_INCREMENT_PER"));
        r.setNewBasicSalary(getBd(rs, "NEW_BASIC_SALARY"));
        r.setNewFaAlw(getBd(rs, "NEW_FA_ALW"));
        r.setNewTaAlw(getBd(rs, "NEW_TA_ALW"));
        r.setNewHraAlw(getBd(rs, "NEW_HRA_ALW"));
        r.setNewFixotAlw(getBd(rs, "NEW_FIXOT_ALW"));
        r.setNewSplAlw(getBd(rs, "NEW_SPL_ALW"));
        r.setNewOthAlw(getBd(rs, "NEW_OTH_ALW"));
        r.setNewAvgot(getBd(rs, "NEW_AVGOT"));
        r.setNewGrossPay(getBd(rs, "NEW_GROSS_PAY"));
        r.setStatus(rs.getString("STATUS"));
        r.setNetIncrement(getBd(rs, "NET_INCREMENT"));
        r.setLastIncrementDate(toLocalDate(rs, "LAST_INCREMENT_DATE"));
        r.setLastIncrementAmt(getBd(rs, "LAST_INCREMENT_AMT"));
        r.setLastBonus(getBd(rs, "LAST_BONUS"));
        r.setCurTicketPeriod(getBd(rs, "CUR_TICKET_PERIOD"));
        r.setCurNoOfTickets(getBd(rs, "CUR_NO_OF_TICKETS"));
        r.setNewTicketPeriod(getBd(rs, "NEW_TICKET_PERIOD"));
        r.setNewNoOfTickets(getBd(rs, "NEW_NO_OF_TICKETS"));
        r.setNewDesignationPoid(getLong(rs, "NEW_DESIGNATION_POID"));
        r.setArrears(getBd(rs, "ARREARS"));
        r.setNewBonusPer(getBd(rs, "NEW_BONUS_PER"));
        r.setLetterEmailedOn(toLocalDate(rs, "LETTER_EMAILED_ON"));
        r.setGridListingMethod(rs.getString("GRID_LISTING_METHOD"));
        r.setRegisteredSalary(getBd(rs, "REGISTERED_SALARY"));
        r.setCurMonthlyCtc(getBd(rs, "CUR_MONTHLY_CTC"));
        r.setCurYearlyCtc(getBd(rs, "CUR_YEARLY_CTC"));
        r.setNewMonthlyCtc(getBd(rs, "NEW_MONTHLY_CTC"));
        r.setNewYearlyCtc(getBd(rs, "NEW_YEARLY_CTC"));
        r.setLastDesignationPoid(getLong(rs, "LAST_DESIGNATION_POID"));
        r.setLastPromotionDate(toLocalDate(rs, "LAST_PROMOTION_DATE"));
        r.setCreatedBy(rs.getString("CREATED_BY"));
        r.setCreatedDate(toLocalDateTime(rs, "CREATED_DATE"));
        r.setLastmodifiedBy(rs.getString("LASTMODIFIED_BY"));
        r.setLastmodifiedDate(toLocalDateTime(rs, "LASTMODIFIED_DATE"));
        r.setEmployeeCode(rs.getString("EMPLOYEE_CODE"));
        r.setEmployeeName(rs.getString("EMPLOYEE_NAME"));
        r.setEmployeeName2(rs.getString("EMPLOYEE_NAME2"));
        return r;
    }

    private static BigDecimal getBd(ResultSet rs, String col) throws SQLException {
        BigDecimal v = rs.getBigDecimal(col);
        return v != null ? v.setScale(3, RoundingMode.HALF_UP) : null;
    }

    private static Long getLong(ResultSet rs, String col) throws SQLException {
        long v = rs.getLong(col);
        return rs.wasNull() ? null : v;
    }

    private static LocalDate toLocalDate(ResultSet rs, String col) throws SQLException {
        java.sql.Date d = rs.getDate(col);
        return d != null ? d.toLocalDate() : null;
    }

    private static LocalDateTime toLocalDateTime(ResultSet rs, String col) throws SQLException {
        java.sql.Timestamp ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private Map<String, Object> params(Object... values) {
        Map<String, Object> out = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            out.put(String.valueOf(values[i]), values[i + 1]);
        }
        return out;
    }

    private Map<String, Object> computeTotals(List<HrAppraisalDtlResponse> details) {
        BigDecimal curGross = details.stream().map(d -> nz(d.getCurGrossPay())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal newGross = details.stream().map(d -> nz(d.getNewGrossPay())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal increase = newGross.subtract(curGross);
        BigDecimal increasePct = curGross.compareTo(BigDecimal.ZERO) != 0
                ? increase.divide(curGross, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal bonus = details.stream().map(d -> nz(d.getNewBonus())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal arrears = details.stream().map(d -> nz(d.getArrears())).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> totals = new HashMap<>();
        totals.put("totalGrossCurrent", curGross);
        totals.put("totalGrossNew", newGross);
        totals.put("totalIncrease", increase);
        totals.put("totalIncreasePercent", increasePct);
        totals.put("totalBonus", bonus);
        totals.put("totalArrears", arrears);
        totals.put("recordsCount", (long) details.size());
        return totals;
    }

    private void appendRefreshedData(Map<String, Object> result, Long transactionPoid) {
        List<HrAppraisalDtlResponse> details = fetchDetailsWithEmployeeData(transactionPoid, null, null, null, null);
        result.put("details", details);
        result.put("totals", computeTotals(details));
        result.put("totalRecords", details.size());
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

    private void validateFinancialYear(LocalDate transactionDate, LocalDate existingTransactionDate) {
        String financialYearResult = jdbcTemplate.queryForObject(
                "SELECT FUNC_GLOB_FINANCIAL_YEAR_VALID(?, ?) FROM DUAL",
                String.class,
                UserContext.getCompanyPoid(),
                transactionDate
        );
        if (financialYearResult != null && financialYearResult.toUpperCase().contains("ERROR")) {
            throw new ValidationException("Changes allowed only within current Financial Period.");
        }
        if (existingTransactionDate != null && !existingTransactionDate.equals(transactionDate)) {
            String transactionYearResult = jdbcTemplate.queryForObject(
                    "SELECT FUNC_GLOB_TRANSACTN_YEAR_VALID(?, ?) FROM DUAL",
                    String.class,
                    UserContext.getCompanyPoid(),
                    transactionDate
            );
            if (transactionYearResult != null && transactionYearResult.toUpperCase().contains("ERROR")) {
                throw new ValidationException("Transaction date cannot be updated.");
            }
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
