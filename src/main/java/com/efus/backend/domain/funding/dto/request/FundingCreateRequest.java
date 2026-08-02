package com.efus.backend.domain.funding.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record FundingCreateRequest(

        @NotBlank(message = "행사명은 필수입니다.")
        String name,

        @NotNull(message = "예산은 필수입니다.")
        @Positive(message = "예산은 0보다 커야 합니다.")
        Long budgetAmount,

        @NotNull(message = "참여 인원은 필수입니다.")
        @Positive(message = "참여 인원은 0보다 커야 합니다.")
        Integer participantCount,

        @NotNull(message = "시작일은 필수입니다.")
        LocalDate startDate,

        @NotNull(message = "종료일은 필수입니다.")
        LocalDate endDate
) {
}