package com.efus.backend.domain.transaction.dto.request;

import com.efus.backend.domain.transaction.entity.TransactionType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionUpdateRequest {

    private TransactionType transactionType;

    private String title;

    @Positive(message = "거래 금액은 0보다 커야 합니다.")
    private Long amount;

    private LocalDate transactionDate;

    private JsonNode fundingId;

    private JsonNode memo;

    // PATCH 필드 미전달 | 기존 값 유지
    public boolean hasFundingId() {
        return fundingId != null;
    }

    // PATCH nullable 필드에 null 전달 | 기존 연결 또는 값 제거
    public boolean isFundingIdNull() {
        return hasFundingId() && fundingId.isNull();
    }

    // PATCH 필드 제대로 전달
    public Long getFundingIdValue() {
        if (!hasFundingId() || isFundingIdNull()) {
            return null;
        }

        return fundingId.asLong();
    }

    // PATCH 필드 미전달 | 기존 값 유지
    public boolean hasMemo() {
        return memo != null;
    }

    // PATCH nullable 필드에 null 전달 | 기존 연결 또는 값 제거
    public boolean isMemoNull() {
        return hasMemo() && memo.isNull();
    }

    // PATCH 필드 제대로 전달
    public String getMemoValue() {
        if (!hasMemo() || isMemoNull()) {
            return null;
        }

        return memo.asText();
    }
}