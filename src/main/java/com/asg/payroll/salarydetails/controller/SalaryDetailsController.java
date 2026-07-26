package com.asg.payroll.salarydetails.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDownloadHeaderService;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.salarydetails.dto.SalaryDetailRequest;
import com.asg.payroll.salarydetails.dto.SalaryDetailResponse;
import com.asg.payroll.salarydetails.service.SalaryDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.error;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequestMapping("/v1/salary-details")
@Tag(name = "Salary Details", description = "APIs for managing employee salary details and history")
@RequiredArgsConstructor
@Slf4j
public class SalaryDetailsController {

    private final LoggingService loggingService;
    private final SalaryDetailsService service;
    private final DocumentDownloadHeaderService downloadHeaderService;

    private static final String FAILEDTOGENERATECONTRACTPDF = "Failed to generate contract PDF: ";

    @Operation(summary = "Update Salary Details")
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSalaryDetail(
            @PathVariable Long id,
            @Valid @RequestBody SalaryDetailRequest request) {
        SalaryDetailResponse response = service.update(id, request);
        return success("Salary details updated successfully", response);
    }

    @Operation(summary = "Get Salary Details by ID")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{id}")
    public ResponseEntity<?> getSalaryDetailById(@PathVariable Long id) {
        SalaryDetailResponse response = service.getById(id);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), id.toString());
        return success("Salary details fetched successfully", response);
    }

    @Operation(summary = "List Salary Details with Filters")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> listSalaryDetails(
            @ParameterObject Pageable pageable,
            @RequestBody(required = false) FilterRequestDto filterRequest) {
        Map<String, Object> result = service.list(filterRequest, pageable);
        return success("Salary details list fetched successfully", result);
    }

    @Operation(summary = "Delete Salary Details")
    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSalaryDetail(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) DeleteReasonDto deleteReason) {
        service.delete(id, deleteReason);
        return success("Salary details deleted successfully");
    }

    @Operation(summary = "Add Salary to History")
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{id}/add-to-history")
    public ResponseEntity<?> addToHistory(@PathVariable Long id) {
        String status = service.addToHistory(id);
        if (status != null && status.startsWith("SUCCESS")) {
            return success("Salary details added to history successfully");
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", status != null ? status : "Unknown error"));
        }
    }

    @Operation(summary = "Sync HR Data from Production to Payroll")
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/sync-hr-data")
    public ResponseEntity<?> syncHRData() {
        String status = service.syncHRData();
        if (status != null && status.startsWith("SUCCESS")) {
            return success(status);
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", status != null ? status : "Unknown error"));
        }
    }

    @Operation(summary = "Calculate CTC for Employee")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/employee/{empPoid}/ctc")
    public ResponseEntity<?> calculateCTC(@PathVariable Long empPoid) {
        String ctc = service.calculateCTC(empPoid);
        return success("CTC calculated successfully", Map.of("ctc", ctc));
    }

    @Operation(summary = "Print Offer Letter")
    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/{id}/print-offer-letter")
    public ResponseEntity<?> printOfferLetter(@PathVariable Long id) {
        try {
            byte[] res = service.printOfferLetter(id);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(
                            UserContext.getDocumentId(), id, "offer-letter", "pdf"))
                    .contentType(MediaType.APPLICATION_PDF).body(res);
        } catch (Exception e) {
            log.error("Failed to generate offer letter: {}", id, e);
            return error(FAILEDTOGENERATECONTRACTPDF + e.getMessage(), 500);
        }

    }

    @Operation(summary = "Print Salary Certificate")
    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/{id}/print-salary-certificate")
    public ResponseEntity<?> printSalaryCertificate(@PathVariable Long id) {

        try {
            byte[] res = service.printSalaryCertificate(id);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(
                            UserContext.getDocumentId(), id, "salary-certificate", "pdf"))
                    .contentType(MediaType.APPLICATION_PDF).body(res);
        } catch (Exception e) {
            log.error("Failed to generate salary certificate: {}", id, e);
            return error(FAILEDTOGENERATECONTRACTPDF + e.getMessage(), 500);
        }
    }

    @Operation(summary = "Print Contract")
    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/{id}/print-contract")
    public ResponseEntity<?> printContract(@PathVariable Long id, @RequestParam String contractPrintType) {

        try {
            byte[] res = service.printContract(id, contractPrintType);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(
                            UserContext.getDocumentId(), id, "contract", "pdf"))
                    .contentType(MediaType.APPLICATION_PDF).body(res);
        } catch (Exception e) {
            log.error("Failed to generate contract: {}", id, e);
            return error(FAILEDTOGENERATECONTRACTPDF + e.getMessage(), 500);
        }
    }

    @Operation(summary = "Print Annex")
    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/{id}/print-annex")
    public ResponseEntity<?> printAnnex(@PathVariable Long id) {
        try {
            byte[] res = service.printAnnex(id);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(
                            UserContext.getDocumentId(), id, "annex", "pdf"))
                    .contentType(MediaType.APPLICATION_PDF).body(res);
        } catch (Exception e) {
            log.error("Failed to generate annex: {}", id, e);
            return error("Failed to generate annex PDF: " + e.getMessage(), 500);
        }
    }

    @Operation(summary = "Print Employee Details with Salary")
    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/{id}/print-employee-details")
    public ResponseEntity<?> printEmployeeDetails(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean preview) {
        try {
            byte[] res = service.printEmployeeDetails(id, preview);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(
                            UserContext.getDocumentId(), id, "employee-details", "pdf"))
                    .contentType(MediaType.APPLICATION_PDF).body(res);
        } catch (Exception e) {
            log.error("Failed to generate employee details report: {}", id, e);
            return error("Failed to generate employee details PDF: " + e.getMessage(), 500);
        }
    }

    @Operation(summary = "View Salary Revisions for Employee")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{id}/salary-revisions")
    public ResponseEntity<?> viewSalaryRevisions(@PathVariable Long id) {
        Map<String, Object> result = service.getSalaryRevisions(id);
        return success("Salary revisions fetched successfully", result);
    }

    @Operation(summary = "Enable Salary Edit Mode")
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{id}/enable-edit")
    public ResponseEntity<?> enableSalaryEdit(@PathVariable Long id) {
        service.enableSalaryEdit(id);
        return success("Salary edit mode enabled. Please refresh the salary table to apply changes.");
    }

}
