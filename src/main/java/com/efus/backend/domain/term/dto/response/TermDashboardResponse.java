package com.efus.backend.domain.term.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;

public record TermDashboardResponse(
        TermInfoDto term,
        FinancialSummaryDto financialSummary,
        List<RecentLedgerEntryDto> recentLedgerEntries,
        List<FundingBudgetDto> fundingBudgets
) {

    public record TermInfoDto(
            Long termId,
            String organizationName,
            String name,
            String status,
            String myRole
    ) {
    }

    public record FinancialSummaryDto(
            Long totalIncome,
            Long totalExpense,
            Long balance,
            Double usageRate
    ) {
        public static FinancialSummaryDto of(Long totalIncome, Long totalExpense) {
            long safeIncome = (totalIncome != null) ? totalIncome : 0L;
            long safeExpense = (totalExpense != null) ? totalExpense : 0L;

            long balance = safeIncome - safeExpense;

            double usageRate = (safeIncome == 0L) ? 0.0 : ((double) safeExpense / safeIncome) * 100.0;

            return new FinancialSummaryDto(
                    safeIncome,
                    safeExpense,
                    balance,
                    Math.round(usageRate * 10.0) / 10.0
            );
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecentLedgerEntryDto(
            String entryType,
            Long entryId,
            String cashFlowType,
            LocalDate transactionDate,
            Long fundingId,
            String fundingName,
            String title,
            Long amount,
            Long requestedAmount,
            Long paidAmount,
            Long unpaidAmount,
            String paymentStatus,
            Boolean deleted
    ) {
    }

    public record FundingBudgetDto(
            Long fundingId,
            String name,
            Long budgetAmount,
            Long spentAmount,
            Long remainingAmount,
            Double usageRate,
            String budgetStatus,
            Long exceededAmount
    ) {
        public static FundingBudgetDto of(Long fundingId, String name, Long budgetAmount, Long spentAmount) {
            long safeBudget = (budgetAmount != null) ? budgetAmount : 0L;
            long safeSpent = (spentAmount != null) ? spentAmount : 0L;

            long remainingAmount = safeBudget - safeSpent;
            long exceededAmount = Math.max(safeSpent - safeBudget, 0L);

            double usageRate = 0.0;
            String budgetStatus = "NORMAL";

            if (safeBudget == 0L && safeSpent == 0L) {
                usageRate = 0.0;
                budgetStatus = "NORMAL";
            } else if (safeBudget == 0L && safeSpent > 0L) {
                usageRate = 0.0;
                budgetStatus = "EXCEEDED";
            } else {
                usageRate = ((double) safeSpent / safeBudget) * 100.0;

                if (usageRate > 100.0) {
                    budgetStatus = "EXCEEDED";
                } else if (usageRate >= 80.0) {
                    budgetStatus = "WARNING";
                } else {
                    budgetStatus = "NORMAL";
                }
            }

            return new FundingBudgetDto(
                    fundingId,
                    name,
                    safeBudget,
                    safeSpent,
                    remainingAmount,
                    Math.round(usageRate * 10.0) / 10.0,
                    budgetStatus,
                    exceededAmount
            );
        }
    }
}
