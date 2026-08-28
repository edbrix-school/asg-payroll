package com.asg.payroll.employeeSettlement.service.impl;

import com.asg.common.lib.dto.*;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.*;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.payroll.employeeSettlement.dto.EmployeeSettlementDto;
import com.asg.payroll.employeeSettlement.entity.EmployeeSettlementDtl;
import com.asg.payroll.employeeSettlement.entity.LoanDeductionDtl;
import com.asg.payroll.exceptions.ValidationException;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.*;
import com.asg.payroll.employeeSettlement.repository.EmployeeSettlementDtlRepository;
import com.asg.payroll.employeeSettlement.repository.LoanDeductionDtlRepository;
import com.asg.payroll.employeeSettlement.service.EmployeeSettlementService;
import com.asg.payroll.employeeSettlement.util.EmployeeSettlementMapper;
import com.asg.payroll.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeSettlementServiceImpl implements EmployeeSettlementService {

    private final EmployeeSettlementDtlRepository employeeSettlementDtlRepository;
    private final LoanDeductionDtlRepository loanDeductionDtlRepository;
    private final DocumentDeleteService documentDeleteService;
    private final LoggingService loggingService;
    private final LovDataService lovDataService;
    private final JdbcTemplate jdbcTemplate;
    private final DocumentSearchService documentService;
    private final PrintService printService;
    private final DataSource dataSource;

    @PersistenceContext
    private final EntityManager entityManager;
    private static final String EMPLOYEE_SettleMENT = "EmployeeSettlement";
    private static final String TRANSACTION_POID = "TRANSACTION_POID";

    @Override
    @Transactional(readOnly = true)
    public EmployeeSettlementDto getEmployeeSettlement(Long id) {
        log.info("Getting Employee Settlement with id: {}", id);
        EmployeeSettlementDtl entity = employeeSettlementDtlRepository.findByTransactionPoid(id).orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_POID, EMPLOYEE_SettleMENT, id.toString()));
        List<LoanDeductionDtl> loanDeductionDetails = loanDeductionDtlRepository.findByTransactionPoidOrderByDetRowId(id);
        EmployeeSettlementDto dto = EmployeeSettlementMapper.mapToDto(entity, lovDataService);
        dto.setLoanDeductionDetails(EmployeeSettlementMapper.mapLoanDtlListToDto(loanDeductionDetails));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> searchEmployeeSettlement(String docId, FilterRequestDto request, Pageable pageable, LocalDate startDate, LocalDate endDate) {
        log.info("startdate", startDate, endDate);
        log.info("Searching Employee Settelment records with docId: {}, page: {}, size: {}", docId, pageable.getPageNumber(), pageable.getPageSize());
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveDateFilters(request, "TRANSACTION_DATE", startDate, endDate);
        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted, "DOC_REF", "TRANSACTION_POID");
        com.asg.payroll.common.util.SearchResultUtil.normalizeDates(raw);
        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());
        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    @Transactional
    public void deleteEmployeeSettlement(Long id, DeleteReasonDto deleteReasonDto) {
        log.info("Deleted Successfully for the Poid", id);
        EmployeeSettlementDtl existingData = employeeSettlementDtlRepository.findByTransactionPoid(id).orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_POID, EMPLOYEE_SettleMENT, id.toString()));
        documentDeleteService.deleteDocument(id, "HR_LEAVE_SETTLEMENT_HDR", TRANSACTION_POID, deleteReasonDto, existingData.getTransactionDate());
    }

    @Override
    @Transactional
    public EmployeeSettlementDto createEmployeeSettlement(EmployeeSettlementDto dto) {
        log.info("Creating employee settlement for employeePoid: {}", dto.getEmployeePoid());
        validateDateOverlap(null, dto);
        EmployeeSettlementDtl entity = new EmployeeSettlementDtl();
        EmployeeSettlementMapper.mapCreateDtoToEntity(dto, entity);
        EmployeeSettlementDtl saved = employeeSettlementDtlRepository.saveAndFlush(entity);
        entityManager.refresh(saved);
        if (dto.getLoanDeductionDetails() != null && !dto.getLoanDeductionDetails().isEmpty()) {
            List<LoanDeductionDtl> loanDetails = EmployeeSettlementMapper.mapLoanDtlListFromDto(dto.getLoanDeductionDetails(), saved.getTransactionPoid());
            loanDeductionDtlRepository.saveAll(loanDetails);
        }
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), entity.getTransactionPoid().toString(), String.format("%s %s", LogDetailsEnum.CREATED.getDescription(), entity.getDocRef()));
        return getEmployeeSettlement(entity.getTransactionPoid());
    }

    @Override
    @Transactional
    public EmployeeSettlementDto updateEmployeeSettlement(Long transactionPoid, EmployeeSettlementDto dto) {
        log.info("Updating Employee Settlement for id : {}", transactionPoid);
        EmployeeSettlementDtl existingEntity = employeeSettlementDtlRepository.findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_POID, EMPLOYEE_SettleMENT, transactionPoid.toString()));
        if ("Y".equals(existingEntity.getDeleted())) {
            throw new ResourceNotFoundException(EMPLOYEE_SettleMENT, TRANSACTION_POID, transactionPoid.toString());
        }
        validateDateOverlap(transactionPoid, dto);
        EmployeeSettlementDtl oldEntity = new EmployeeSettlementDtl();
        BeanUtils.copyProperties(existingEntity, oldEntity);
        EmployeeSettlementMapper.mapUpdateDtoToEntity(dto, existingEntity);
        employeeSettlementDtlRepository.save(existingEntity);
        saveLoanDeductionDetails(transactionPoid, dto);
        loggingService.logChanges(oldEntity, existingEntity, EmployeeSettlementDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), LogDetailsEnum.MODIFIED, transactionPoid.toString());
        log.info("Employee Settlement updated successfully");
        return getEmployeeSettlement(transactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getEmployeeEligibleLeave(Long id) {
        EmployeeSettlementDtl entity = employeeSettlementDtlRepository.findByTransactionPoid(id).orElseThrow(() -> new ResourceNotFoundException(TRANSACTION_POID, EMPLOYEE_SettleMENT, id.toString()));
        try {
            Long userPoid = UserContext.getUserPoid();
            String sql = "{call PROC_HR_ELIGIBLELEAVE_DAYS(?, ?, ?, ?, ?, ?, ?, ?)}";
            return jdbcTemplate.execute((ConnectionCallback<Object>) connection -> {
                CallableStatement cs = connection.prepareCall(sql);
                cs.setLong(1, entity.getCompanyPoid());
                cs.setLong(2, entity.getEmployeePoid());
                cs.setDate(3, entity.getLeaveStartDate() != null ? Date.valueOf(entity.getLeaveStartDate()) : null);
                cs.setNull(4, Types.DATE);
                cs.setObject(5, entity.getTransactionPoid());
                if (entity.getLeaveAbsentDays() != null) {
                    cs.setLong(6, entity.getLeaveAbsentDays().longValue());
                } else {
                    cs.setNull(6, Types.NUMERIC);
                }
                cs.registerOutParameter(7, OracleTypes.CURSOR);
                cs.registerOutParameter(8, Types.VARCHAR);
                cs.execute();
                String status = cs.getString(8);
                if (status != null && status.startsWith("ERRROR")) {
                    throw new ValidationException(status);
                }
                ResultSet rs = (ResultSet) cs.getObject(7);
                Map<String, Object> result = new HashMap<>();
                if (rs != null && rs.next()) {
                    result = new ColumnMapRowMapper().mapRow(rs, 1);
                }
                rs.close();
                cs.close();
                return result;
            });
        } catch (Exception e) {
            log.error("Error getting employee eligible leave", e);
            throw new ValidationException("Error getting employee eligible leave: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object getEmployeeEligibleLeaveByParams(Long companyPoid, Long employeePoid, LocalDate leaveStartDate, Long settlementPoid, Long leaveAbsentDays) {
        try {
            String sql = "{call PROC_HR_ELIGIBLELEAVE_DAYS(?, ?, ?, ?, ?, ?, ?, ?)}";
            return jdbcTemplate.execute((ConnectionCallback<Object>) connection -> {
                CallableStatement cs = connection.prepareCall(sql);
                cs.setLong(1, companyPoid);
                cs.setLong(2, employeePoid);
                cs.setDate(3, Date.valueOf(leaveStartDate));
                cs.setNull(4, Types.DATE);
                if (settlementPoid != null) {
                    cs.setLong(5, settlementPoid);
                } else {
                    cs.setNull(5, Types.NUMERIC);
                }
                if (leaveAbsentDays != null) {
                    cs.setLong(6, leaveAbsentDays);
                } else {
                    cs.setNull(6, Types.NUMERIC);
                }
                cs.registerOutParameter(7, OracleTypes.CURSOR);
                cs.registerOutParameter(8, Types.VARCHAR);
                cs.execute();
                String status = cs.getString(8);
                if (status != null && status.startsWith("ERRROR")) {
                    throw new ValidationException(status);
                }
                ResultSet rs = (ResultSet) cs.getObject(7);
                Map<String, Object> result = new HashMap<>();
                if (rs != null && rs.next()) {
                    result = new ColumnMapRowMapper().mapRow(rs, 1);
                }
                rs.close();
                cs.close();
                return result;
            });
        } catch (Exception e) {
            log.error("Error getting employee eligible leave", e);
            throw new ValidationException("Error getting employee eligible leave: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String createSettlementBpv(Long id, String paymentType, Long bankPoid, String payeeName, LocalDate paymentValueDate, String prePrinted) {
        try {
            String sql = "{call PROC_HR_SETTLEMENT_CREATE_BPV(?,?,?,?,?,?,?,?)}";
            return jdbcTemplate.execute((ConnectionCallback<String>) connection -> {
                CallableStatement cs = connection.prepareCall(sql);
                cs.setLong(1, UserContext.getUserPoid());
                cs.setLong(2, id);
                cs.setString(3, paymentType);
                cs.setLong(4, bankPoid);
                cs.setString(5, payeeName);
                cs.setDate(6, Date.valueOf(paymentValueDate));
                cs.setString(7, prePrinted);
                cs.registerOutParameter(8, Types.VARCHAR);
                cs.execute();
                String status = cs.getString(8);
                cs.close();
                if (status != null && status.toUpperCase().contains("ERROR")) {
                    throw new ValidationException(status);
                }
                return status;
            });
        } catch (Exception e) {
            log.error("Error creating settlement BPV", e);
            throw new ValidationException("Error creating settlement BPV : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String createSettlementBdv(Long id, String paymentType, Long bankPoid, String payeeName, LocalDate paymentValueDate, String prePrinted) {
        try {
            String sql = "{call PROC_HR_SETTLEMENT_CREATE_BDV(?,?,?,?,?,?,?,?)}";
            return jdbcTemplate.execute((ConnectionCallback<String>) connection -> {
                CallableStatement cs = connection.prepareCall(sql);
                cs.setLong(1, UserContext.getUserPoid());
                cs.setLong(2, id);
                cs.setString(3, paymentType);
                cs.setLong(4, bankPoid);
                cs.setString(5, payeeName);
                cs.setDate(6, Date.valueOf(paymentValueDate));
                cs.setString(7, prePrinted);
                cs.registerOutParameter(8, Types.VARCHAR);
                cs.execute();
                String status = cs.getString(8);
                cs.close();
                if (status != null && status.toUpperCase().contains("ERROR")) {
                    throw new ValidationException(status);
                }
                return status;
            });
        } catch (Exception e) {
            log.error("Error creating settlement BPV", e);
            throw new ValidationException("Error creating settlement BPV : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String createSettlementJv(Long id) {
        try {
            String sql = "{call PROC_HR_SETTLEMENT_CREATE_JV(?,?,?)}";
            return jdbcTemplate.execute((ConnectionCallback<String>) connection -> {
                CallableStatement cs = connection.prepareCall(sql);
                cs.setLong(1, UserContext.getUserPoid());
                cs.setLong(2, id);
                cs.registerOutParameter(3, Types.VARCHAR);
                cs.execute();
                String status = cs.getString(3);
                cs.close();
                log.info("Settlement JV Status : {}", status);
                if (status != null && status.toUpperCase().contains("ERROR")) {
                    throw new ValidationException(status);
                }
                return status;
            });
        } catch (Exception e) {
            log.error("Error creating settlement JV", e);
            throw new ValidationException("Error creating settlement JV : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, String> getEmployeeLeaveDates(String employeePoid) {
        try {
            String sql = "{call PROC_HR_EMP_LEAVE_DATES(?,?,?,?,?)}";
            return jdbcTemplate.execute((ConnectionCallback<Map<String, String>>) connection -> {
                CallableStatement cs = connection.prepareCall(sql);
                cs.setLong(1, UserContext.getGroupPoid());
                cs.setLong(2, UserContext.getCompanyPoid());
                cs.setString(3, employeePoid);
                cs.registerOutParameter(4, Types.VARCHAR);
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                Map<String, String> response = new HashMap<>();
                response.put("startDate", cs.getString(4));
                response.put("rejoinDate", cs.getString(5));
                cs.close();
                return response;
            });
        } catch (Exception e) {
            log.error("Error fetching employee leave dates", e);
            throw new ValidationException("Error fetching employee leave dates : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, Object> getLeaveRequestDetails(Long id) {
        try {
            String sql = "{call PROC_HR_LEAVE_REQ_DETAILS(?,?,?,?)}";
            return jdbcTemplate.execute((ConnectionCallback<Map<String, Object>>) connection -> {
                CallableStatement cs = connection.prepareCall(sql);
                cs.setLong(1, UserContext.getCompanyPoid());
                cs.setLong(2, id);
                cs.registerOutParameter(3, OracleTypes.CURSOR);
                cs.registerOutParameter(4, Types.VARCHAR);
                cs.execute();
                Map<String, Object> response = new HashMap<>();
                response.put("status", cs.getString(4));
                ResultSet rs = (ResultSet) cs.getObject(3);
                if (rs != null && rs.next()) {
                    response.put("data", new ColumnMapRowMapper().mapRow(rs, 1));
                }
                cs.close();
                return response;
            });
        } catch (Exception e) {
            log.error("Error fetching leave request details", e);
            throw new ValidationException("Error fetching leave request details : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, Object> calculateIndemnity(Long companyPoid, Long settlementPoid, Long employeePoid, LocalDate settlementDate, Long withoutPayDays) {
        try {
            String sql = "{call PROC_HR_INDEMNITY_DAYS_V2(?,?,?,?,?,?,?)}";
            return jdbcTemplate.execute((ConnectionCallback<Map<String, Object>>) connection -> {
                CallableStatement stmt = connection.prepareCall(sql);
                stmt.setLong(1, companyPoid);
                stmt.setLong(2, settlementPoid);
                stmt.setLong(3, employeePoid);
                stmt.setObject(4, settlementDate != null ? Date.valueOf(settlementDate) : null);
                stmt.setObject(5, withoutPayDays);
                stmt.registerOutParameter(6, Types.VARCHAR);
                stmt.registerOutParameter(7, OracleTypes.CURSOR);
                stmt.execute();
                Map<String, Object> response = new HashMap<>();
                response.put("status", stmt.getString(6));
                ResultSet rs = (ResultSet) stmt.getObject(7);
                List<Map<String, Object>> dataList = new ArrayList<>();
                if (rs != null) {
                    dataList = new RowMapperResultSetExtractor<>(new ColumnMapRowMapper()).extractData(rs);
                    rs.close();
                }
                response.put("data", dataList);
                stmt.close();
                log.info("calculateIndemnity response status: {}", response.get("status"));
                return response;
            });
        } catch (Exception e) {
            log.error("Error calculating indemnity", e);
            throw new ValidationException("Error calculating indemnity : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, Object> getRecurringToPayroll(Long payrollPoid, Long settlementPoid, Long empPoid, LocalDate payrollDate) {
        try {
            String sql = "{call PROC_HR_RECURRING_TO_PAYROLL(?,?,?,?,?)}";
            return jdbcTemplate.execute((ConnectionCallback<Map<String, Object>>) connection -> {
                CallableStatement cs = connection.prepareCall(sql);
                cs.setNull(1, Types.NUMERIC);
                if (settlementPoid != null) {
                    cs.setLong(2, settlementPoid);
                } else {
                    cs.setNull(2, Types.NUMERIC);
                }
                if (empPoid != null) {
                    cs.setLong(3, empPoid);
                } else {
                    cs.setNull(3, Types.NUMERIC);
                }
                if (payrollDate != null) {
                    cs.setDate(4, Date.valueOf(payrollDate));
                } else {
                    cs.setNull(4, Types.DATE);
                }
                cs.registerOutParameter(5, OracleTypes.CURSOR);
                cs.execute();
                ResultSet rs = (ResultSet) cs.getObject(5);
                Map<String, Object> response = new HashMap<>();
                List<Map<String, Object>> dataList = new RowMapperResultSetExtractor<>(new ColumnMapRowMapper()).extractData(rs);
                response.put("data", dataList);
                cs.close();
                return response;
            });
        } catch (Exception e) {
            log.error("Error fetching recurring payroll data", e);
            throw new ValidationException("Error fetching recurring payroll data : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, Object> processLeavePayroll(Long companyPoid, Long settlementTranPoid, Long attendTrnsPoid, Long attend2TrnsPoid, Long empPoid, LocalDate finalDateOfWork, LocalDate leaveEndDate, Long loanDedAmt) {
        try {
            String sql = "{call PROC_HR_PAYROLL_PROCESS_LEAVE(?,?,?,?,?,?,?,?,?,?,?)}";
            return jdbcTemplate.execute((ConnectionCallback<Map<String, Object>>) connection -> {
                CallableStatement cs = connection.prepareCall(sql);
                cs.setLong(1, companyPoid);
                if (settlementTranPoid != null) {
                    cs.setLong(2, settlementTranPoid);
                } else {
                    cs.setNull(2, Types.NUMERIC);
                }
                cs.setObject(3, attendTrnsPoid);
                cs.setObject(4, attend2TrnsPoid);
                cs.setLong(5, empPoid);
                cs.setObject(6, finalDateOfWork != null ? Date.valueOf(finalDateOfWork) : null);
                cs.setObject(7, leaveEndDate != null ? Date.valueOf(leaveEndDate) : null);
                cs.setNull(8, Types.NUMERIC);
                cs.registerOutParameter(9, Types.VARCHAR);
                cs.registerOutParameter(10, OracleTypes.CURSOR);
                cs.registerOutParameter(11, OracleTypes.CURSOR);
                cs.execute();
                Map<String, Object> response = new HashMap<>();
                String status = cs.getString(9);
                response.put("status", status);
                if (status != null && status.toUpperCase().contains("ERROR")) {
                    throw new ValidationException(status);
                }
                ResultSet rs1 = (ResultSet) cs.getObject(10);
                if (rs1 != null) {
                    List<Map<String, Object>> output1 = new RowMapperResultSetExtractor<>(new ColumnMapRowMapper()).extractData(rs1);
                    response.put("data1", output1);
                    rs1.close();
                }
                ResultSet rs2 = (ResultSet) cs.getObject(11);
                if (rs2 != null) {
                    List<Map<String, Object>> output2 = new RowMapperResultSetExtractor<>(new ColumnMapRowMapper()).extractData(rs2);
                    response.put("data2", output2);
                    rs2.close();
                }
                cs.close();
                return response;
            });
        } catch (Exception e) {
            log.error("Error processing leave payroll", e);
            throw new ValidationException("Error processing leave payroll : " + e.getMessage());
        }
    }

    @Override
    public String syncHRData() {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("SYNC_HR_PRODUCTION_TO_PAYROLL");
            query.registerStoredProcedureParameter("P_STATUS", String.class, ParameterMode.OUT);
            query.execute();
            return (String) query.getOutputParameterValue("P_STATUS");
        } catch (Exception e) {
            log.error("Error executing SYNC_HR_PRODUCTION_TO_PAYROLL: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    @Override
    public byte[] printSettlement(Long transactionPoid) throws JRException {
        return generateSettlementPdf("HR/HrSettlement.jrxml", transactionPoid);
    }

    @Override
    public byte[] printSettlementAmtDetailsForBank(Long transactionPoid) throws JRException {
        return generateSettlementPdf("HR/HrSettlement_AmtDetailsForBank.jrxml", transactionPoid);
    }

    @Override
    public byte[] printSettlementRetirementLetterForBank(Long transactionPoid) throws JRException {
        return generateSettlementPdf("HR/HrSettlement_RetirementLetterForBank.jrxml", transactionPoid);
    }

    private byte[] generateSettlementPdf(String reportFile, Long transactionPoid) throws JRException {
        Map<String, Object> params = printService.buildBaseParams(transactionPoid, UserContext.getDocumentId());
        params.put("SUBREPORT_DIR", getCompiledSubreportDir());
        JasperReport mainReport = printService.load(reportFile);
        try {
            return printService.fillReportToPdf(mainReport, params, dataSource);
        } catch (JRException e) {
            throw e;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    private static volatile String compiledSubreportDir;

    private String getCompiledSubreportDir() throws JRException {
        if (compiledSubreportDir != null) return compiledSubreportDir;
        synchronized (EmployeeSettlementServiceImpl.class) {
            if (compiledSubreportDir != null) return compiledSubreportDir;
            try {
                java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("jasper_root");
                java.nio.file.Path templatesDir = tempDir.resolve("Templates");
                java.nio.file.Files.createDirectories(templatesDir);
                String[] subreports = {"DocHeaderSubReport", "DocFooterSubReport", "DocFooterSubReport-ISO"};
                for (String name : subreports) {
                    try (java.io.InputStream in = getClass().getClassLoader()
                            .getResourceAsStream("jasper/Templates/" + name + ".jrxml")) {
                        if (in != null) {
                            net.sf.jasperreports.engine.JasperReport compiled =
                                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(in);
                            net.sf.jasperreports.engine.util.JRSaver.saveObject(compiled,
                                    templatesDir.resolve(name + ".jasper").toFile());
                        }
                    }
                }
                compiledSubreportDir = tempDir.toAbsolutePath() + java.io.File.separator;
            } catch (Exception e) {
                throw new JRException("Failed to compile subreports: " + e.getMessage(), e);
            }
        }
        return compiledSubreportDir;
    }

    private void saveLoanDeductionDetails(Long transactionPoid, EmployeeSettlementDto dto) {
        if (dto.getLoanDeductionDetails() == null) {
            return;
        }
        loanDeductionDtlRepository.deleteByTransactionPoid(transactionPoid);
        if (!dto.getLoanDeductionDetails().isEmpty()) {
            List<LoanDeductionDtl> loanDetails = EmployeeSettlementMapper.mapLoanDtlListFromDto(dto.getLoanDeductionDetails(), transactionPoid);
            loanDeductionDtlRepository.saveAll(loanDetails);
        }
    }

    private void validateDateOverlap(Long transactionPoid, EmployeeSettlementDto dto) {
        if (dto == null) {
            return;
        }

        LocalDate startDate = dto.getLeaveStartDate();
        LocalDate endDate = dto.getLeaveEndDate();

        if (startDate != null && endDate == null) {
            throw new ValidationException("ToDateFieldValue parameter required for overlap checking...");
        }

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new ValidationException("Leave Starting Date should be before the rejoin date...");
            }

            if (dto.getEmployeePoid() != null) {
                Long groupPoid = UserContext.getGroupPoid() != null ? UserContext.getGroupPoid() : 1L;
                Long companyPoid = UserContext.getCompanyPoid() != null ? UserContext.getCompanyPoid() : 1L;
                String customWhereClause = "EMPLOYEE_POID = '" + dto.getEmployeePoid() + "' ";
                Long poidVal = transactionPoid != null ? transactionPoid : 0L;

                checkOverlapStoredProc(groupPoid, companyPoid, "HR_LEAVE_SETTLEMENT_HDR", "LEAVE_START_DATE", "LEAVE_END_DATE",
                        startDate, endDate, "TRANSACTION_POID", poidVal, customWhereClause);

                String docId = UserContext.getDocumentId() != null ? UserContext.getDocumentId() : "800-103";
                String historyWhereClause = customWhereClause + " AND SOURCE_DOC_ID <> '" + docId + "' ";
                checkOverlapStoredProc(groupPoid, companyPoid, "HR_EMPLOYEE_LEAVE_HISTORY", "LEAVE_START_DATE", "REJOIN_DATE",
                        startDate, endDate, "SOURCE_DOC_POID", poidVal, historyWhereClause);
            }
        }
    }

    private void checkOverlapStoredProc(Long groupPoid, Long companyPoid, String tableName, String fromDateField, String toDateField,
                                        LocalDate fromDate, LocalDate toDate, String poidField, Long poidValue, String scope) {
        if (dataSource == null) {
            return;
        }
        String sql = "{? = call FUNC_GLOB_DATE_OVERLAP_CHECK(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                return;
            }
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                if (stmt == null) {
                    return;
                }
                stmt.registerOutParameter(1, Types.VARCHAR);
                stmt.setObject(2, groupPoid);
                stmt.setObject(3, companyPoid);
                stmt.setString(4, tableName);
                stmt.setString(5, fromDateField);
                stmt.setString(6, toDateField);
                stmt.setDate(7, java.sql.Date.valueOf(fromDate));
                stmt.setDate(8, java.sql.Date.valueOf(toDate));
                stmt.setString(9, poidField);
                stmt.setLong(10, poidValue);
                stmt.setString(11, scope);

                stmt.execute();
                String result = stmt.getString(1);
                if (result != null && !result.toUpperCase().contains("SUCCESS")) {
                    throw new ValidationException("Period selected is overlapping with some existing period, please check...");
                }
            }
        } catch (SQLException e) {
            log.error("Error executing FUNC_GLOB_DATE_OVERLAP_CHECK for table {}", tableName, e);
        }
    }
}
