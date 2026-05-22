package com.asg.payroll.salarydetails.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.dto.request.LogRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ResourceNotFoundException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.PrintService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryMaster;
import com.asg.payroll.salarydetails.repository.HrEmployeeSalaryMasterRepository;
import com.asg.payroll.exceptions.ValidationException;
import com.asg.payroll.salarydetails.dto.SalaryAllowanceDto;
import com.asg.payroll.salarydetails.dto.SalaryDetailRequest;
import com.asg.payroll.salarydetails.dto.SalaryDetailResponse;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryAlwDtl;
import com.asg.payroll.salarydetails.entity.HrEmployeeSalaryHist;
import com.asg.payroll.salarydetails.repository.HrEmployeeSalaryAlwDtlRepository;
import com.asg.payroll.salarydetails.repository.HrEmployeeSalaryHistRepository;
import com.asg.payroll.salarydetails.repository.HrEmployeeSalaryProcRepository;
import com.asg.payroll.salarydetails.service.SalaryDetailsService;
import com.asg.payroll.salarydetails.util.AllowanceProcessingContext;
import com.asg.payroll.salarydetails.util.SalaryDetailsMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryDetailsServiceImpl implements SalaryDetailsService {

    private final HrEmployeeSalaryMasterRepository repository;
    private final HrEmployeeSalaryAlwDtlRepository alwDtlRepository;
    private final HrEmployeeSalaryHistRepository histRepository;
    private final HrEmployeeSalaryProcRepository procRepository;

    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;
    private final PrintService printService;
    private final DataSource dataSource;

    private static final String SALARY_POID = "SALARY_POID";
    private static final String EMPLOYEE_POID = "EMPLOYEE_POID";
    private static final String RESOURCE_NAME = "Salary Details";

    @Override
    @Transactional
    public SalaryDetailResponse update(Long id, SalaryDetailRequest request) {
        HrEmployeeSalaryMaster entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, SALARY_POID, id));

        validate(request);

        HrEmployeeSalaryMaster oldEntity = new HrEmployeeSalaryMaster();
        // Manually copy properties if BeanUtils not preferred or for specific fields
        BeanUtils.copyProperties(entity, oldEntity);

        SalaryDetailsMapper.mapToEntity(request, entity);

        calculateTotals(entity, request.getAllowances());

        repository.save(entity);
        saveAllowances(id, request.getAllowances());


        String docId = UserContext.getDocumentId();

        loggingService.logChanges(oldEntity, entity, HrEmployeeSalaryMaster.class, docId, id.toString(), LogDetailsEnum.MODIFIED, SALARY_POID);

        return getById(id);
    }

    @Override
    public SalaryDetailResponse getById(Long id) {
        HrEmployeeSalaryMaster entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, SALARY_POID, id));

        return buildSalaryResponse(entity);
    }


    @Override
    @Transactional
    public void delete(Long id, DeleteReasonDto deleteReasonDto) {
        repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, SALARY_POID, id));
        documentDeleteService.deleteDocument(id, "HR_EMPLOYEE_SALARY_MASTER", SALARY_POID, deleteReasonDto, null);
    }

    @Override
    public Map<String, Object> list(FilterRequestDto filterRequest, Pageable pageable) {
        String operator = documentSearchService.resolveOperator(filterRequest);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequest);
        List<FilterDto> filterList = documentSearchService.resolveFilters(filterRequest);

        RawSearchResult raw = documentSearchService.search(UserContext.getDocumentId(), filterList, operator, pageable,
                isDeleted, "EMPLOYEE_NAME", SALARY_POID);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());
        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    public String addToHistory(Long salaryPoid) {
        return procRepository.addToSalaryHistory(UserContext.getCompanyPoid(), UserContext.getUserPoid(), salaryPoid);
    }

    @Override
    public String syncHRData() {
        String status = procRepository.syncHRData();
        loggingService.createLogSummaryEntry(LogDetailsEnum.MODIFIED, UserContext.getDocumentId(), "Sync HR Data");
        return status;
    }

    @Override
    public String calculateCTC(Long employeePoid) {
        return procRepository.calculateCTC(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), employeePoid);
    }

    @Override
    public byte[] printOfferLetter(Long id) throws Exception {
        String docId = UserContext.getDocumentId();
        Map<String, Object> params = printService.buildBaseParams(id, docId);
        JasperReport mainReport = printService.load("HR/EmployeeOfferLetter.jrxml");
        return printService.fillReportToPdf(mainReport, params, dataSource);
    }

    @Override
    public byte[] printSalaryCertificate(Long id) throws Exception {
        String docId = UserContext.getDocumentId();
        Map<String, Object> params = printService.buildBaseParams(id, docId);
        JasperReport mainReport = printService.load("HR/EmployeeSalaryCertificate.jrxml");
        return printService.fillReportToPdf(mainReport, params, dataSource);
    }

    @Override
    public byte[] printContract(Long id, String contractPrintType) throws Exception {
        String docId = UserContext.getDocumentId();
        Map<String, Object> params = printService.buildBaseParams(id, docId);
        String printOption = contractPrintType.toUpperCase();
        String reportFileName = "HR/Employee_Contract_Annex_one.jrxml";
        switch (printOption) {
            case "EXPAT_OPEN":
                reportFileName = "HR/Employee_Contract_Open_expat.jrxml";
                break;
            case "EXPAT_LTD":
                reportFileName = "HR/Employee_Contract_Limited_expat.jrxml";
                break;
            case "BAHRAINI_OPEN":
                reportFileName = "HR/Employee_Contract_Open_Bahrainis.jrxml";
                break;
            case "BAHRAINI_LTD":
                reportFileName = "HR/Employee_Contract_Limited_bahraini.jrxml";
                break;
            default:
                break;
        }
        JasperReport mainReport = printService.load(reportFileName);
        return printService.fillReportToPdf(mainReport, params, dataSource);
    }

    private SalaryDetailResponse buildSalaryResponse(HrEmployeeSalaryMaster entity) {

        Long salaryPoid = entity.getSalaryPoid();

        List<HrEmployeeSalaryAlwDtl> allowances =
                alwDtlRepository.findBySalaryPoid(salaryPoid);

        List<HrEmployeeSalaryHist> history =
                histRepository.findBySalaryPoid(salaryPoid);

        SalaryDetailResponse response =
                SalaryDetailsMapper.mapToResponse(entity, allowances, history);

        // Fetch read-only fields
        Map<String, Object> employeeDetails =
                procRepository.getEmployeeDetails(entity.getEmployeePoid());

        if (employeeDetails != null) {
            response.setTicketDetails((String) employeeDetails.get("TICKET_DETAILS"));
            response.setDesignation((String) employeeDetails.get("DESIGNATION_NAME"));

            Object joinDate = employeeDetails.get("JOIN_DATE");
            if (joinDate instanceof java.sql.Timestamp ts) {
                response.setJoinDate(ts.toLocalDateTime().toLocalDate());
            } else if (joinDate instanceof java.time.LocalDateTime ldt) {
                response.setJoinDate(ldt.toLocalDate());
            }
        }

        response.setCtcAmount(calculateCTC(entity.getEmployeePoid()));

        return response;
    }

    private void validate(SalaryDetailRequest request) {
        if (request.getEmployeePoid() == null) throw new ValidationException("Employee is mandatory");
        if (request.getPaymentMethod() == null) throw new ValidationException("Payment Method is mandatory");
        if (request.getPaymentMethod().equalsIgnoreCase("bank") && request.getBankPoid() == null) throw new ValidationException("Bank is mandatory");
        if (request.getPaymentMethod().equalsIgnoreCase("bank") && request.getBankRegistrationId() == null) throw new ValidationException("Bank Reg ID is mandatory");

        if (request.getIbanAccountNo() != null && request.getIbanAccountNo().length() != 22) {
            throw new ValidationException("IBAN Account number should be 22 characters");
        }
    }

    private void calculateTotals(HrEmployeeSalaryMaster entity, List<SalaryAllowanceDto> allowances) {
        BigDecimal basic = entity.getBasicSalary() != null ? entity.getBasicSalary() : BigDecimal.ZERO;
        BigDecimal totalAllowance = BigDecimal.ZERO;
        if (allowances != null) {
            totalAllowance = allowances.stream()
                    .filter(a -> a.getActive() != null && a.getActive() == 1L)
                    .map(a -> a.getAmount() != null ? a.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        entity.setTotAllowance(totalAllowance);
        entity.setGrossSalary(basic.add(totalAllowance));
        entity.setNetSalary(entity.getGrossSalary()); // Simplified, deductions not in SRS yet
    }

    private void saveAllowances(Long salaryPoid, List<SalaryAllowanceDto> allowanceDtos) {

        if (allowanceDtos == null || allowanceDtos.isEmpty()) {
            return;
        }

        String docId = UserContext.getDocumentId();
        String docKeyPoid = salaryPoid.toString();

        List<HrEmployeeSalaryAlwDtl> toSave = new ArrayList<>();
        List<HrEmployeeSalaryAlwDtl> toUpdate = new ArrayList<>();
        List<Long> toDelete = new ArrayList<>();
        List<LogRequestDto<HrEmployeeSalaryAlwDtl>> logRequests = new ArrayList<>();

        Long maxDetRowId = getMaxDetRowId(salaryPoid);

        AllowanceProcessingContext ctx = new AllowanceProcessingContext(
                salaryPoid, maxDetRowId, docId, docKeyPoid,
                toSave, toUpdate, toDelete, logRequests
        );

        for (SalaryAllowanceDto dto : allowanceDtos) {
            processAllowanceDto(dto, ctx);
        }

        persistAllowanceChanges(
                salaryPoid,
                ctx.getToSave(),
                ctx.getToUpdate(),
                ctx.getToDelete(),
                ctx.getLogRequests(),
                docId,
                docKeyPoid
        );
    }

    private void processAllowanceDto(SalaryAllowanceDto dto, AllowanceProcessingContext ctx) {

        String actionType = String.valueOf(dto.getActionType());

        switch (actionType) {

            case "ISCREATED":
                Long newRowId = dto.getDetRowId() != null
                        ? dto.getDetRowId()
                        : incrementDetRowId(ctx);

                HrEmployeeSalaryAlwDtl entity =
                        createAllowance(dto, ctx.getSalaryPoid(), newRowId);

                ctx.getToSave().add(entity);
                break;

            case "ISUPDATED":
                handleUpdate(dto,
                        ctx.getSalaryPoid(),
                        ctx.getToUpdate(),
                        ctx.getLogRequests(),
                        ctx.getDocId(),
                        ctx.getDocKeyPoid());
                break;

            case "ISDELETED":
                handleDelete(dto,
                        ctx.getToDelete(),
                        ctx.getDocId(),
                        ctx.getDocKeyPoid());
                break;
            default:
                break;
        }
    }

    private HrEmployeeSalaryAlwDtl createAllowance(
            SalaryAllowanceDto dto,
            Long salaryPoid,
            Long detRowId) {

        HrEmployeeSalaryAlwDtl entity = new HrEmployeeSalaryAlwDtl();
        SalaryDetailsMapper.mapDtoToAllowanceEntity(dto, entity, salaryPoid);
        entity.setDetRowId(detRowId);
        return entity;
    }


    private void handleUpdate(
            SalaryAllowanceDto dto,
            Long salaryPoid,
            List<HrEmployeeSalaryAlwDtl> toUpdate,
            List<LogRequestDto<HrEmployeeSalaryAlwDtl>> logRequests,
            String docId,
            String docKeyPoid) {

        HrEmployeeSalaryAlwDtl existingData = alwDtlRepository
                .findBySalaryPoidAndDetRowId(salaryPoid, dto.getDetRowId())
                .orElseThrow(() -> new ValidationException(
                        "Allowance detail not found for detRowId: " + dto.getDetRowId()));

        HrEmployeeSalaryAlwDtl oldEntity = new HrEmployeeSalaryAlwDtl();
        BeanUtils.copyProperties(existingData, oldEntity);

        HrEmployeeSalaryAlwDtl updated = new HrEmployeeSalaryAlwDtl();
        BeanUtils.copyProperties(existingData, updated);

        SalaryDetailsMapper.mapDtoToAllowanceEntity(dto, updated, salaryPoid);

        toUpdate.add(updated);

        logRequests.add(new LogRequestDto<>(
                oldEntity,
                updated,
                HrEmployeeSalaryAlwDtl.class,
                docId,
                docKeyPoid,
                "EMPLOYEESALARYALLOWANCE DET_ROW_ID: " + dto.getDetRowId()
        ));
    }


    private void handleDelete(
            SalaryAllowanceDto dto,
            List<Long> toDelete,
            String docId,
            String docKeyPoid) {

        toDelete.add(dto.getDetRowId());
        loggingService.logDelete(dto, docId, docKeyPoid);
    }

    private Long incrementDetRowId(AllowanceProcessingContext ctx) {
        Long updated = ctx.getMaxDetRowId() + 1;
        ctx.setMaxDetRowId(updated);
        return updated;
    }

    private Long getMaxDetRowId(Long salaryPoid) {
        Long max = alwDtlRepository.getMaxDetRowId(salaryPoid);
        return max != null ? max : 0L;
    }

    private void persistAllowanceChanges(
            Long salaryPoid,
            List<HrEmployeeSalaryAlwDtl> toSave,
            List<HrEmployeeSalaryAlwDtl> toUpdate,
            List<Long> toDelete,
            List<LogRequestDto<HrEmployeeSalaryAlwDtl>> logRequests,
            String docId,
            String docKeyPoid) {

        if (!toSave.isEmpty()) {
            List<HrEmployeeSalaryAlwDtl> saved = alwDtlRepository.saveAll(toSave);

            saved.forEach(e -> loggingService.createLogSummaryEntry(
                    docId,
                    docKeyPoid,
                    "HR Employee Salary Allowance detail created with detRowId: " + e.getDetRowId()
            ));
        }

        if (!toUpdate.isEmpty()) {
            alwDtlRepository.saveAll(toUpdate);

            if (!logRequests.isEmpty()) {
                loggingService.createLogBatch(logRequests);
            }
        }

        if (!toDelete.isEmpty()) {
            alwDtlRepository.deleteBySalaryPoidAndDetRowIdIn(salaryPoid, toDelete);
        }
    }


}
