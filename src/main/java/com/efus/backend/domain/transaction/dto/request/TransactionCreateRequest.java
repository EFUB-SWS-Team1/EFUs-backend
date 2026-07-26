package com.efus.backend.domain.transaction.dto.request;

import com.efus.backend.domain.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record TransactionCreateRequest(

        @NotNull(message = "거래 유형은 필수입니다.")
        TransactionType transactionType,

        @NotBlank(message = "거래 제목은 필수입니다.")
        String title,

        @NotNull(message = "거래 금액은 필수입니다.")
        @Positive(message = "거래 금액은 0보다 커야 합니다.")
        Long amount,

        @Positive(message = "행사 ID는 0보다 커야 합니다.")
        Long fundingId,

        @NotNull(message = "거래 날짜는 필수입니다.")
        LocalDate transactionDate,

        String memo
) {
}