package com.efus.backend.domain.funding.dto.request;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record FundingUpdateRequest(
        String name,
        @Positive(message = "예산은 0보다 커야 합니다.") Long budgetAmount,
        @Positive(message = "참여 인원은 0보다 커야 합니다.") Integer participantCount,
        LocalDate startDate,
        LocalDate endDate
) {
}
