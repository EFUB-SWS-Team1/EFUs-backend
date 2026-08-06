package com.efus.backend.domain.funding.dto.response;

public record FundingSummaryResponse(
        Long termId,
        Long totalBudgetAmount,
        Long totalSpentAmount,
        Long totalRemainingAmount,
        Long fundingCount
) {
    public static FundingSummaryResponse of(
            Long termId,
            Long totalBudgetAmount,
            Long totalSpentAmount,
            Long fundingCount
    ) {
        long safeBudget = (totalBudgetAmount != null) ? totalBudgetAmount : 0L;
        long safeSpent = (totalSpentAmount != null) ? totalSpentAmount : 0L;
        long safeCount = (fundingCount != null) ? fundingCount : 0L;

        // 잔액 계산: 총예산 - 총지출
        long remainingAmount = safeBudget - safeSpent;

        return new FundingSummaryResponse(
                termId,
                safeBudget,
                safeSpent,
                remainingAmount,
                safeCount
        );
    }
}