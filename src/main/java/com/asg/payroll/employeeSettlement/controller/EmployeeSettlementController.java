package com.asg.payroll.employeeSettlement.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.employeeSettlement.dto.EmployeeSettlementDto;
import com.asg.payroll.employeeSettlement.entity.EmployeeSettlementDtl;
import com.asg.payroll.employeeSettlement.service.EmployeeSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/employee-settlement")
@Slf4j
public class EmployeeSettlementController {

    private final EmployeeSettlementService service;
    private final LoggingService loggingService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(summary = "Get Employee Settlement Details by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Employee Settlement Fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Employee Settlement not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeDetailById(@PathVariable Long id) {
        EmployeeSettlementDto response = service.getEmployeeSettlement(id);
        return success("Employee Settlement details fetched successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @Operation(summary = "Delete a Employee Settlement", description = "Soft deletes a Employee Settlement record", responses = {
            @ApiResponse(responseCode = "200", description = "Employee Settlement deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee Settlement not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployeeSettlement(
            @Parameter(description = "Transaction POID", required = true, example = "5001") @PathVariable Long id,
            @Valid @RequestBody(required = false) DeleteReasonDto deleteReason) {
        log.info("Delete request for Employee Settlement with id: {}", id);
        service.deleteEmployeeSettlement(id, deleteReason);
        loggingService.createLogSummaryEntry(LogDetailsEnum.DELETED, UserContext.getDocumentId(), id.toString());
        return success("Employee Settlement deleted successfully");
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @Operation(summary = "Create a new Employee Settlement", description = "Creates a new Employee Settlement", responses = {
            @ApiResponse(responseCode = "200", description = "Employee Settlement created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<?> createEmployeeSettlement(@Valid @RequestBody EmployeeSettlementDto createDTO) {
        log.info("Create request for Employee Settlement");
        EmployeeSettlementDto dto = service.createEmployeeSettlement(createDTO);
        return success("Employee Settlement created successfully", dto);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @Operation(summary = "Update an existing Employee Settlement", description = "Updates the EmployeeSettlement details for the given Transaction POID", responses = {
            @ApiResponse(responseCode = "200", description = "EmployeeSettlement updated successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "EmployeeSettlement not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployeeSettlement(
            @Parameter(description = "Transaction POID", required = true, example = "5001") @PathVariable Long id,
            @Valid @RequestBody EmployeeSettlementDto updateDTO) {
        log.info("Update request for EmployeeSettlement with id: {}", id);
        service.updateEmployeeSettlement(id, updateDTO);
        EmployeeSettlementDto dto = service.getEmployeeSettlement(id);
        return success("EmployeeSettlement updated successfully", dto);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(summary = "Get all EmployeeSettlement", description = "Fetches all EmployeeSettlement records for the given group", responses = {
            @ApiResponse(responseCode = "200", description = "Employee Settlement records fetched successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/search")
    public ResponseEntity<?> searchEmployeeSettlement(@ParameterObject Pageable pageable,
                                                      @RequestBody(required = false) FilterRequestDto filters,
                                                      @RequestParam(required = false) LocalDate startDate,
                                                      @RequestParam(required = false) LocalDate endDate) {
        log.info("Search request for EmployeeSettlement with docId: {}", UserContext.getDocumentId());
        if ((startDate == null && endDate != null) || (startDate != null && endDate == null)) {
            return badRequest("Both startDate and endDate should be specified or both dates should be empty.");
        }
        Map<String, Object> result = service.searchEmployeeSettlement(UserContext.getDocumentId(), filters, pageable, startDate, endDate);
        return success("Employee Settlement records retrieved successfully", result);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(summary = "Get Employee Eligible Leave Days", responses = {
            @ApiResponse(responseCode = "200", description = "Eligible Leave Fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Employee Settlement not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/eligible-leave-days/{id}")
    public ResponseEntity<?> getEmployeeEligibileleave(@PathVariable Long id) {
        Object response = service.getEmployeeEligibleLeave(id);
        return success("Employee Eligible Leave fetched successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(summary = "Get Employee Eligible Leave Days by Params", responses = {
            @ApiResponse(responseCode = "200", description = "Eligible Leave Fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/eligible-leave-days")
    public ResponseEntity<?> getEmployeeEligibleLeaveByParams(
            @RequestParam Long companyPoid,
            @RequestParam Long employeePoid,
            @RequestParam LocalDate leaveStartDate,
            @RequestParam(required = false) Long settlementPoid,
            @RequestParam(required = false) Long leaveAbsentDays) {
        Object response = service.getEmployeeEligibleLeaveByParams(companyPoid, employeePoid, leaveStartDate, settlementPoid, leaveAbsentDays);
        return success("Employee Eligible Leave fetched successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @Operation(summary = "Create Settlement BPV", responses = {
            @ApiResponse(responseCode = "200", description = "Settlement BPV created successfully"),
            @ApiResponse(responseCode = "404", description = "Employee Settlement not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/create-bpv/{id}")
    public ResponseEntity<?> createSettlementBpv(@PathVariable Long id, @RequestBody EmployeeSettlementDtl request) {
        String status = service.createSettlementBpv(
                id,
                request.getPaymentDocType(),
                request.getPaymentBankPoid(),
                request.getPaymentPayeeName(),
                request.getPaymentValueDate(),
                request.getPaymentPrePrinted()
        );
        return success(status);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @Operation(summary = "Create Settlement BDV", responses = {
            @ApiResponse(responseCode = "200", description = "Settlement BDV created successfully"),
            @ApiResponse(responseCode = "404", description = "Employee Settlement not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/create-bdv/{id}")
    public ResponseEntity<?> createSettlementBdv(@PathVariable Long id, @RequestBody EmployeeSettlementDtl request) {
        String status = service.createSettlementBdv(
                id,
                request.getPaymentDocType(),
                request.getPaymentBankPoid(),
                request.getPaymentPayeeName(),
                request.getPaymentValueDate(),
                request.getPaymentPrePrinted()
        );
        return success(status);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @Operation(summary = "Create Settlement JV", responses = {
            @ApiResponse(responseCode = "200", description = "Settlement JV created successfully"),
            @ApiResponse(responseCode = "404", description = "Employee Settlement not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/create-jv/{id}")
    public ResponseEntity<?> createSettlementJV(@PathVariable Long id) {
        String status = service.createSettlementJv(id);
        return success("Created the Settlement JV Successfully", status);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(summary = "Get Employee Leave Datas", responses = {
            @ApiResponse(responseCode = "200", description = "Employee Leave Datas Fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Employee Settlement not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/leave-dates/{employeePoid}")
    public ResponseEntity<?> getEmployeeLeaveDates(@PathVariable String employeePoid) {
        Map<String, String> response = service.getEmployeeLeaveDates(employeePoid);
        return success("Employee Leave Data Fetched Successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(summary = "Get Employee Leave Request Details", responses = {
            @ApiResponse(responseCode = "200", description = "Leave details fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Leave request not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/leave-request-details/{id}")
    public ResponseEntity<?> getLeaveRequestDetails(@PathVariable Long id) {
        Map<String, Object> response = service.getLeaveRequestDetails(id);
        return success("Leave Request Details Fetched Successfully", response.get("data"));
    }

    @Operation(summary = "Sync HR Data from Production to Payroll")
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping("/sync-hr-data")
    public ResponseEntity<?> syncHRData() {
        String status = service.syncHRData();
        if (status != null && status.startsWith("SUCCESS")) {
            return success(status);
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", status != null ? status : "Unknown error"));
        }
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @Operation(summary = "Get Recurring Payroll Details", responses = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")}, security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/recurring-payroll/{payrollPoid}")
    public ResponseEntity<?> getRecurringPayroll(
            @PathVariable Long payrollPoid,
            @RequestParam(required = false) Long settlementPoid,
            @RequestParam(required = false) Long empPoid,
            @RequestParam(required = false) LocalDate payrollDate) {
        Map<String, Object> response = service.getRecurringToPayroll(payrollPoid, settlementPoid, empPoid, payrollDate);
        return success("Recurring Payroll Data Fetched Successfully", response.get("data"));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/calculate-indemnity")
    public ResponseEntity<?> calculateIndemnity(
            @RequestParam Long companyPoid,
            @RequestParam Long settlementPoid,
            @RequestParam Long employeePoid,
            @RequestParam LocalDate settlementDate,
            @RequestParam(defaultValue = "0") Long withoutPayDays) {
        Map<String, Object> response = service.calculateIndemnity(companyPoid, settlementPoid, employeePoid, settlementDate, withoutPayDays);
        return success("Indemnity Calculated Successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/process-leave-payroll")
    public ResponseEntity<?> processLeavePayroll(
            @RequestParam Long companyPoid,
            @RequestParam(required = false) Long settlementTranPoid,
            @RequestParam(required = false) Long attendTrnsPoid,
            @RequestParam(required = false) Long attend2TrnsPoid,
            @RequestParam Long empPoid,
            @RequestParam LocalDate finalDateOfWork,
            @RequestParam(required = false) LocalDate leaveEndDate,
            @RequestParam(defaultValue = "0") Long loanDedAmt) {
        Map<String, Object> response = service.processLeavePayroll(companyPoid, settlementTranPoid, attendTrnsPoid, attend2TrnsPoid, empPoid, finalDateOfWork, leaveEndDate, loanDedAmt);
        return success("Payroll Processed Successfully", response);
    }

    @GetMapping("/printSettlement/{transactionPoid}")
    public ResponseEntity<?> printSettlement(
            @Parameter(description = "Transaction POID", example = "71031")
            @PathVariable Long transactionPoid) {
        try {
            byte[] pdf = service.printSettlement(transactionPoid);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=purchase-journal-" + transactionPoid + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return internalServerError("Failed to generate PDF: " + e.getMessage());
        }
    }

    @GetMapping("/printSettleAmtDtlBnk/{transactionPoid}")
    public ResponseEntity<?> printSettlementAmtDetailsForBank(
            @Parameter(description = "Transaction POID", example = "71031")
            @PathVariable Long transactionPoid) {
        try {
            byte[] pdf = service.printSettlementAmtDetailsForBank(transactionPoid);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=purchase-journal-" + transactionPoid + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return internalServerError("Failed to generate PDF: " + e.getMessage());
        }
    }

    @GetMapping("/printSettleRetirtLtrBnk/{transactionPoid}")
    public ResponseEntity<?> printSettlementRetirementLetterForBank(
            @Parameter(description = "Transaction POID", example = "71031")
            @PathVariable Long transactionPoid) {
        try {
            byte[] pdf = service.printSettlementRetirementLetterForBank(transactionPoid);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=purchase-journal-" + transactionPoid + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return internalServerError("Failed to generate PDF: " + e.getMessage());
        }
    }
}
