package com.asg.payroll.payrollprocess.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.payroll.payrollprocess.dto.HrPayrollHdrRequest;
import com.asg.payroll.payrollprocess.dto.LoadLoansAdvancesRequest;
import com.asg.payroll.payrollprocess.dto.LoadVariablesRequest;
import com.asg.payroll.payrollprocess.dto.PayrollActionRequest;
import com.asg.payroll.payrollprocess.service.HrPayrollProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.asg.common.lib.dto.response.ApiResponse.error;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequestMapping("/v1/payroll-process")
@RequiredArgsConstructor
public class HrPayrollProcessController {

    private static final String FAILED_TO_GENERATE_PDF = "Failed to generate PDF: ";

    private final HrPayrollProcessService hrPayrollProcessService;
    private final LoggingService loggingService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/list")
    public ResponseEntity<?> listPayrolls(
            @RequestBody(required = false) FilterRequestDto filterRequestDto,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo) {
        return success("Payroll list fetched successfully",
                hrPayrollProcessService.listPayrolls(UserContext.getDocumentId(), filterRequestDto, pageable, periodFrom, periodTo));
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getPayrollById(@PathVariable Long transactionPoid) {
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return success("Payroll retrieved successfully", hrPayrollProcessService.getPayrollById(transactionPoid));
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createPayroll(@Valid @RequestBody HrPayrollHdrRequest request) {
        return success("Payroll created successfully", hrPayrollProcessService.createPayroll(request));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updatePayroll(@PathVariable Long transactionPoid,
                                           @Valid @RequestBody HrPayrollHdrRequest request) {
        return success("Payroll updated successfully", hrPayrollProcessService.updatePayroll(transactionPoid, request));
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deletePayroll(@PathVariable Long transactionPoid,
                                           @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
        hrPayrollProcessService.deletePayroll(transactionPoid, deleteReasonDto);
        return success("Payroll deleted successfully");
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/process-payroll")
    public ResponseEntity<?> processPayroll(@PathVariable Long transactionPoid,
                                            @RequestBody PayrollActionRequest request) {
        return success("Payroll processed successfully", hrPayrollProcessService.processPayroll(transactionPoid, request));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/process-provision")
    public ResponseEntity<?> processProvision(@PathVariable Long transactionPoid,
                                              @RequestBody PayrollActionRequest request,
                                              @RequestParam(required = false, defaultValue = "N") String postJv) {
        return success("Provision processed successfully", hrPayrollProcessService.processProvision(transactionPoid, request, postJv));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/revert")
    public ResponseEntity<?> revertPayroll(@PathVariable Long transactionPoid) {
        return success("Payroll reverted successfully", hrPayrollProcessService.revertPayroll(transactionPoid));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/load-variables")
    public ResponseEntity<?> loadVariables(@PathVariable Long transactionPoid,
                                           @RequestBody LoadVariablesRequest request) {
        return success("Payroll variables loaded successfully", 
                hrPayrollProcessService.loadVariables(transactionPoid, request.getSettlementPoid(), 
                        request.getEmpPoid(), request.getPayrollDate()));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/load-loans-advances")
    public ResponseEntity<?> loadLoansAdvances(@PathVariable Long transactionPoid,
                                               @RequestBody LoadLoansAdvancesRequest request) {
        return success("Loans and advances loaded successfully", 
                hrPayrollProcessService.loadLoansAdvances(transactionPoid, request.getSettlementPoid(), 
                        request.getEmpPoid(), request.getPayrollDate()));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/create-jv")
    public ResponseEntity<?> createJv(@PathVariable Long transactionPoid,
                                      @RequestParam(required = false) String bankCash) {
        return success("JV and Bank DV created successfully", 
                hrPayrollProcessService.createJv(UserContext.getUserPoid(), transactionPoid, bankCash));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/bank-file")
    public ResponseEntity<?> generateBankFile(@PathVariable Long transactionPoid) {
        return success("Bank file generated successfully", hrPayrollProcessService.generateBankFile(transactionPoid));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/hsbc-api-transfer")
    public ResponseEntity<?> hsbcApiTransfer(@PathVariable Long transactionPoid) {
        return success("HSBC API transfer initiated successfully", hrPayrollProcessService.hsbcApiTransfer(transactionPoid));
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/sync-hr-data")
    public ResponseEntity<?> syncHrData() {
        return success("HR data sync completed successfully", hrPayrollProcessService.syncHrData());
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PostMapping("/{transactionPoid}/send-email")
    public ResponseEntity<?> sendEmail(@PathVariable Long transactionPoid,
                                       @RequestBody(required = false) PayrollActionRequest request) {
        return success("Payslip email scheduled successfully",
                hrPayrollProcessService.sendEmail(transactionPoid, request != null ? request : new PayrollActionRequest()));
    }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/print/{transactionPoid}/payslip")
    public ResponseEntity<?> printPayslip(@PathVariable Long transactionPoid) {
        try {
            byte[] pdf = hrPayrollProcessService.printPayslip(transactionPoid);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip-" + transactionPoid + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return error(FAILED_TO_GENERATE_PDF + e.getMessage(), 500);
        }
    }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @GetMapping("/print/{transactionPoid}/preview")
    public ResponseEntity<?> printPreview(@PathVariable Long transactionPoid) {
        try {
            byte[] pdf = hrPayrollProcessService.printPreview(transactionPoid);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payslip-preview-" + transactionPoid + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return error(FAILED_TO_GENERATE_PDF + e.getMessage(), 500);
        }
    }

}
