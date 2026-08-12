package com.asg.payroll.loansadvances.controller;


import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductRequest;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductResponse;
import com.asg.payroll.loansadvances.service.LoansAdvancesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequestMapping("v1/loans-advances")
@Tag(name = "Loans/Advances", description = "APIs for managing loans/advances and related details")
@RequiredArgsConstructor
@Slf4j
public class LoansAdvancesController {

    private final LoggingService loggingService;
    private final LoansAdvancesService service;


    @Operation(
            summary = "Create Loans/Advances",
            description = "Creates a new loan/advance",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Loans/Advances created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HrRecurringPayDeductResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createLoans(
            @Parameter(description = "Loans/Advances details to be created", required = true)
            @Valid @RequestBody HrRecurringPayDeductRequest requestDto) {
        Long transactionPoid = service.create(requestDto);
        return success("Loans/Advances created successfully", Map.of("transactionPoid", transactionPoid));
    }

    @Operation(
            summary = "Update Loans/Advances",
            description = "Updates an existing loans/advances",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Loans/Advances updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input or validation error"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Loans/Advances not found"
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateLoans(
            @Parameter(description = "Loans/Advances Id to update", required = true)
            @PathVariable(name = "transactionPoid") Long transactionPoid,
            @Parameter(description = "Loans/Advances details", required = true)
            @Valid @RequestBody HrRecurringPayDeductRequest requestDto) {

        HrRecurringPayDeductResponse updatedPoid = service.update(transactionPoid, requestDto);
        return success("Loans/Advances updated successfully",(updatedPoid));
    }

    @Operation(
            summary = "Get Loans/Advances by ID",
            description = "Retrieves loans/advances details by transaction POID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Loans/Advances detail retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HrRecurringPayDeductResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Loans/Advances not found"
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getLoanById(
            @Parameter(description = "Loans/Advances ID", required = true)
            @PathVariable(name = "transactionPoid") Long transactionPoid) {
        HrRecurringPayDeductResponse response = service.getById(transactionPoid);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("Loans/Advances detail fetched successfully", response);
    }

    @Operation(
            summary = "List Loans/Advances with Filters & Pagination",
            description = """
                    Fetch Loans/Advances details using pagination + dynamic filters.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = false,
            description = "Search Filters for Loans/Advances",
            content = @Content(
                    schema = @Schema(implementation = FilterRequestDto.class),
                    examples = @ExampleObject(
                            name = "Loans/Advances Filters",
                            value = """
                                    {
                                      "operator": "AND",
                                      "isDeleted": "N",
                                      "filters": [
                                        { "searchField": "ACTIVE", "searchValue": "Y" },
                                        { "searchField": "DESCRIPTIONS", "searchValue": "Test" }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> listLoans(
            @ParameterObject Pageable pageable,
            @RequestBody(required = false) FilterRequestDto filterRequest,
            @Parameter(description = "Period From (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @Parameter(description = "Period To (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo
    ) {
        Map<String, Object> result = service.list(filterRequest, pageable, periodFrom, periodTo);
        return success("Loans/Advances fetched successfully", result);
    }

    @Operation(
            summary = "Delete Loans/Advances",
            description = "Soft deletes a loans/advances",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Loans/Advances deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Loans/Advances not found"
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteLoans(
            @Parameter(description = "Loans/Advances ID to delete", required = true)
            @PathVariable(name = "transactionPoid") Long transactionPoid,
            @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        service.delete(transactionPoid, deleteReasonDto);
        return success("Loan/Advance deleted successfully");
    }

}
