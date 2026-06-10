package com.asg.payroll.loansadvances.util;

import com.asg.common.lib.utility.DateUtil;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductRequest;
import com.asg.payroll.loansadvances.dto.HrRecurringPayDeductResponse;
import com.asg.payroll.loansadvances.entity.HrRecurringPayDeduct;

public class LoansAdvancesMapper {

    private LoansAdvancesMapper(){}

    public static void mapToEntity(HrRecurringPayDeductRequest request, HrRecurringPayDeduct entity) {

        entity.setDocRef(request.getDocRef());
        entity.setTransactionDate(request.getTransactionDate() == null ? DateUtil.getCurrentDateInUserTimeZone() : request.getTransactionDate());
        entity.setEmployeePoid(request.getEmployeePoid());
        entity.setRefNo(request.getRefNo());
        entity.setDescriptions(request.getDescriptions());
        entity.setTotalAmount(request.getTotalAmount());
        entity.setOpeningAmount(request.getOpeningAmount());
        entity.setStartDate(request.getStartDate());
        entity.setMonthlyAmount(request.getMonthlyAmount());
        entity.setSettledAndClosed(request.getSettledAndClosed());
        entity.setReceiptNo(request.getReceiptNo());
        entity.setReceiptDate(request.getReceiptDate());
        entity.setRecurType(request.getRecurType());
        entity.setGlCode(request.getGlCode());
    }

    public static HrRecurringPayDeductResponse mapToResponse(HrRecurringPayDeduct entity) {

        return HrRecurringPayDeductResponse.builder()
                .transactionPoid(entity.getTransactionPoid())
                .groupPoid(entity.getGroupPoid())
                .companyPoid(entity.getCompanyPoid())
                .transactionDate(entity.getTransactionDate())
                .docRef(entity.getDocRef())
//                .recurType(entity.getRecurType())
//                .glCode(entity.getGlCode())
                .employeePoid(entity.getEmployeePoid())
                .refNo(entity.getRefNo())
                .descriptions(entity.getDescriptions())
                .totalAmount(entity.getTotalAmount())
                .openingAmount(entity.getOpeningAmount())
                .startDate(entity.getStartDate())
                .monthlyAmount(entity.getMonthlyAmount())
                .sourceDocId(entity.getSourceDocId())
                .sourceDocPoid(entity.getSourceDocPoid())
                .sourceDocRef(entity.getDocRef())
                .settledAndClosed(entity.getSettledAndClosed())
                .receiptNo(entity.getReceiptNo())
                .receiptDate(entity.getReceiptDate())
                .build();
    }
}
