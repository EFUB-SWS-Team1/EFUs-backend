package com.efus.backend.domain.funding.dto.response;

import com.efus.backend.domain.funding.entity.Funding;

import java.time.LocalDate;
import java.util.List;

public record FundingListResponse(
        List<FundingDetailDto> fundings,
        Long page,
        Long size,
        Long totalElements,
        Long totalPages,
        Boolean first,
        Boolean last
) {
    public record FundingDetailDto(
            Long fundingId,
            String name,
            String startDate,
            String endDate,
            Integer participantCount,
            Long budgetAmount,
            Long spentAmount,
            Long remainingAmount,
            Double usageRate,
            String scheduleStatus,
            String budgetStatus,
            Long exceededAmount
    ) {
        public static FundingDetailDto of(Funding funding, Long spentAmount, LocalDate currentDate) {

            Long budget = funding.getBudgetAmount();
            Long remaining = budget - spentAmount;
            Long exceeded = Math.max(spentAmount - budget, 0L);

            // 사용률 및 예산 상태 계산
            Double usageRate = 0.0;
            String budgetStatus = "NORMAL";

            if (budget == 0L && spentAmount == 0L) {
                usageRate = 0.0;
                budgetStatus = "NORMAL";
            } else if (budget == 0L && spentAmount > 0L) {
                usageRate = 0.0;
                budgetStatus = "EXCEEDED";
            } else {
                usageRate = (double) spentAmount / budget * 100;
                if (usageRate >= 100.0) {
                    budgetStatus = "EXCEEDED";
                } else if (usageRate >= 80.0) {
                    budgetStatus = "WARNING";
                }
            }

            // 일정 상태 계산
            String scheduleStatus;
            if (currentDate.isBefore(funding.getStartDate())) {
                scheduleStatus = "UPCOMING";
            } else if (currentDate.isAfter(funding.getEndDate())) {
                scheduleStatus = "ENDED";
            } else {
                scheduleStatus = "IN_PROGRESS";
            }

            return new FundingDetailDto(
                    funding.getId(),
                    funding.getName(),
                    funding.getStartDate().toString(),
                    funding.getEndDate().toString(),
                    funding.getParticipantCount(),
                    budget,
                    spentAmount,
                    remaining,
                    Math.round(usageRate * 10.0) / 10.0,
                    scheduleStatus,
                    budgetStatus,
                    exceeded
            );
        }
    }
}