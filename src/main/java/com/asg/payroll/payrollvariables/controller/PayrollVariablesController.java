package com.asg.payroll.payrollvariables.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.payrollvariables.dto.PayrollVariablesRequestDTO;
import com.asg.payroll.payrollvariables.dto.PayrollVariablesResponseDTO;
import com.asg.payroll.payrollvariables.service.PayrollVariablesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequestMapping("/v1/payroll-variables")
@RequiredArgsConstructor
@Tag(name = "Payroll Variables / Arrears Entry", description = "APIs for managing Payroll Variables and Arrears Entry")
public class PayrollVariablesController {

    private final PayrollVariablesService service;
    private final LoggingService loggingService;

    @Operation(summary = "Create Payroll Variable / Arrears Entry",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Created successfully",
                            content = @Content(schema = @Schema(implementation = PayrollVariablesResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            })
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PayrollVariablesRequestDTO requestDTO) {
        return success("Payroll Variable created successfully", service.create(requestDTO));
    }

    @Operation(summary = "Update Payroll Variable / Arrears Entry",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated successfully",
                            content = @Content(schema = @Schema(implementation = PayrollVariablesResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "Record not found")
            })
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> update(
            @Parameter(description = "Transaction POID", required = true) @PathVariable Long transactionPoid,
            @Valid @RequestBody PayrollVariablesRequestDTO requestDTO) {
        return success("Payroll Variable updated successfully", service.update(transactionPoid, requestDTO));
    }

    @Operation(summary = "Get Payroll Variable / Arrears Entry by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Fetched successfully",
                            content = @Content(schema = @Schema(implementation = PayrollVariablesResponseDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Record not found")
            })
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getById(
            @Parameter(description = "Transaction POID", required = true) @PathVariable Long transactionPoid) {
        PayrollVariablesResponseDTO response = service.getById(transactionPoid);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("Payroll Variable fetched successfully", response);
    }

    @Operation(summary = "Soft delete Payroll Variable / Arrears Entry",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Record not found")
            })
    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> softDelete(
            @Parameter(description = "Transaction POID", required = true) @PathVariable Long transactionPoid,
            @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        service.softDelete(transactionPoid, deleteReasonDto);
        return success("Payroll Variable deleted successfully");
    }

    @Operation(summary = "List Payroll Variables / Arrears Entries with search, sort and pagination",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List fetched successfully")
            })
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/list")
    public ResponseEntity<?> list(
            @RequestBody(required = false) FilterRequestDto filters,
            @ParameterObject Pageable pageable,
            @Parameter(description = "Period From (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @Parameter(description = "Period To (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo) {
        return success("Payroll Variables fetched successfully",
                service.list(UserContext.getDocumentId(), filters, pageable, periodFrom, periodTo));
    }
}
