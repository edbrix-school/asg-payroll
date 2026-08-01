package com.asg.payroll.employeeappraisal.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDownloadHeaderService;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalActionRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRecalculationRequest;
import com.asg.payroll.employeeappraisal.dto.HrAppraisalRequest;
import com.asg.payroll.employeeappraisal.entity.HrAppraisalHdr;
import com.asg.payroll.employeeappraisal.service.HrAppraisalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.error;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequestMapping("/v1/employee-appraisal")
@RequiredArgsConstructor
public class HrAppraisalController {

    public static final String FAILED_TO_GENERATE_PDF = "Failed to generate PDF: ";

    private final HrAppraisalService hrAppraisalService;
    private final LoggingService loggingService;
    private final DocumentDownloadHeaderService downloadHeaderService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/list")
    public ResponseEntity<?> listAppraisals(@RequestBody(required = false) FilterRequestDto filterRequestDto,
                                            @ParameterObject Pageable pageable,
                                            @RequestParam(required = false) LocalDate periodFrom,
                                            @RequestParam(required = false) LocalDate periodTo) {
        return success("Employee appraisals fetched successfully", hrAppraisalService.listAppraisals(UserContext.getDocumentId(), filterRequestDto, pageable, periodFrom, periodTo));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getAppraisalById(@PathVariable Long transactionPoid) {
        Map<String, Object> response = hrAppraisalService.getAppraisalById(transactionPoid);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("Employee appraisal retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createAppraisal(@Valid @RequestBody HrAppraisalRequest request) {
        return success("Employee appraisal created successfully", hrAppraisalService.createAppraisal(request));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateAppraisal(@PathVariable Long transactionPoid, @Valid @RequestBody HrAppraisalRequest request) {
        return success("Employee appraisal updated successfully", hrAppraisalService.updateAppraisal(transactionPoid, request));
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteAppraisal(@PathVariable Long transactionPoid,
                                             @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        hrAppraisalService.deleteAppraisal(transactionPoid, deleteReasonDto);
        return success("Employee appraisal deleted successfully");
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/employees/{employeePoid}/details")
    public ResponseEntity<?> getDetailsSp(@PathVariable Long transactionPoid, @PathVariable Long employeePoid) {
        Map<String, Object> result = hrAppraisalService.getDetailsSp(transactionPoid, employeePoid);
        return success("Employee appraisal details fetched successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}/details")
    public ResponseEntity<?> getFilteredDetails(
            @PathVariable Long transactionPoid,
            @RequestParam(required = false) Long departmentPoid,
            @RequestParam(required = false) Long designationPoid,
            @RequestParam(required = false) String listingMethod,
            @RequestParam(required = false) String employeeName) {
        return success("Appraisal details fetched successfully",
                hrAppraisalService.getFilteredDetails(transactionPoid, departmentPoid, designationPoid, listingMethod, employeeName));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/load-data")
    public ResponseEntity<?> loadAppraisalData(@PathVariable Long transactionPoid, @RequestBody HrAppraisalActionRequest request) {
        String actionType = request.getLoadActionType() != null
                ? request.getLoadActionType().name()
                : request.getActionType();
        Map<String, Object> result = hrAppraisalService.loadAppraisalDataSp(transactionPoid, actionType);
        return success("Appraisal data load completed", result);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/clear-data")
    public ResponseEntity<?> clearAppraisalData(@PathVariable Long transactionPoid) {
        return success("Appraisal data clear completed", hrAppraisalService.clearAppraisalDataSp(transactionPoid));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/batch-update")
    public ResponseEntity<?> batchUpdate(@PathVariable Long transactionPoid, @RequestBody HrAppraisalActionRequest request) {
        return success("Batch update completed", hrAppraisalService.batchUpdateSp(transactionPoid, request));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/recalculate-detail")
    public ResponseEntity<?> recalculateDetail(@Valid @RequestBody HrAppraisalRecalculationRequest request) {
        return success("Appraisal detail recalculation completed", hrAppraisalService.recalculateDetail(request));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/employees/{employeePoid}/update-data")
    public ResponseEntity<?> updateData(@PathVariable Long transactionPoid, @PathVariable Long employeePoid,
                                        @RequestBody HrAppraisalActionRequest request) {
        return success("Employee appraisal data updated", hrAppraisalService.updateDataSp(transactionPoid, employeePoid, request));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/update-master")
    public ResponseEntity<?> updateMaster(@PathVariable Long transactionPoid, @RequestBody HrAppraisalActionRequest request) {
        return success("Appraisal master update completed", hrAppraisalService.updateMasterSp(transactionPoid, request));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/create-jv")
    public ResponseEntity<?> createJv(@PathVariable Long transactionPoid) {
        return success("JV operation completed", hrAppraisalService.createJvSp(transactionPoid));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/send-email")
    public ResponseEntity<?> sendEmail(@PathVariable Long transactionPoid, @RequestBody(required = false) HrAppraisalActionRequest request) {
        String resend = request == null ? "N" : request.getResend();
        return success("Email schedule operation completed", hrAppraisalService.sendEmailSp(transactionPoid, resend));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/bank-file")
    public ResponseEntity<?> bankFile(@PathVariable Long transactionPoid) {
        return success("Bank file generated successfully", hrAppraisalService.bankFileSp(transactionPoid));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/arrears")
    public ResponseEntity<?> arrears(@PathVariable Long transactionPoid, @RequestBody HrAppraisalActionRequest request) {
        return success("Arrears update completed", hrAppraisalService.arrearsSp(transactionPoid, request.getPayrollPoid()));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/arrears-calc")
    public ResponseEntity<?> arrearsCalc(@PathVariable Long transactionPoid) {
        return success("Arrears calculation completed", hrAppraisalService.arrearsCalcSp(transactionPoid));
    }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/print/{transactionPoid}/a3")
    public ResponseEntity<?> printA3(@PathVariable Long transactionPoid) {
        try {
            byte[] pdf = hrAppraisalService.printA3(transactionPoid);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(
                            HrAppraisalHdr.class, transactionPoid, "appraisal-a3", "pdf"))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return error(FAILED_TO_GENERATE_PDF + e.getMessage(), 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/print/{transactionPoid}/by-company")
    public ResponseEntity<?> printByCompany(@PathVariable Long transactionPoid) {
        try {
            byte[] pdf = hrAppraisalService.printByCompany(transactionPoid);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(
                            HrAppraisalHdr.class, transactionPoid, "appraisal-by-company", "pdf"))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return error(FAILED_TO_GENERATE_PDF + e.getMessage(), 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/print/{transactionPoid}/bank")
    public ResponseEntity<?> printBank(@PathVariable Long transactionPoid) {
        try {
            byte[] pdf = hrAppraisalService.printBank(transactionPoid);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(
                            HrAppraisalHdr.class, transactionPoid, "appraisal-bank", "pdf"))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return error(FAILED_TO_GENERATE_PDF + e.getMessage(), 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/print/{transactionPoid}/letter")
    public ResponseEntity<?> printLetter(@PathVariable Long transactionPoid,
                                         @RequestParam(required = false) Long employeePoid) {
        try {
            byte[] pdf = hrAppraisalService.printLetter(transactionPoid, employeePoid);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(
                            HrAppraisalHdr.class, transactionPoid, "appraisal-letter", "pdf"))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return error(FAILED_TO_GENERATE_PDF + e.getMessage(), 500);
        }
    }

}
