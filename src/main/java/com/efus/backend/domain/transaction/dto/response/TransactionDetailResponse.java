package com.efus.backend.domain.transaction.dto.response;

import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.entity.TransactionType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionDetailResponse(
        Long transactionId,
        Long termId,
        Long fundingId,
        String fundingName,
        TransactionType transactionType,
        String title,
        Long amount,
        LocalDate transactionDate,
        String memo,
        Long createdByTermMemberId,
        String createdByMemberName,
        Long updatedByTermMemberId,
        String updatedByMemberName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TransactionDetailResponse from(Transaction transaction) {
        return new TransactionDetailResponse(
                transaction.getId(),
                transaction.getTerm().getId(),
                transaction.getFunding() == null ? null : transaction.getFunding().getId(),
                transaction.getFunding() == null
                        ? null
                        : transaction.getFunding().getName(),
                transaction.getTransactionType(),
                transaction.getTitle(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getMemo(),
                transaction.getCreatedByTermMember().getId(),
                transaction.getCreatedByTermMember().getUser().getName(),
                transaction.getUpdatedByTermMember() == null ? null : transaction.getUpdatedByTermMember().getId(),
                transaction.getUpdatedByTermMember() == null
                        ? null
                        : transaction.getUpdatedByTermMember().getUser().getName(),
                transaction.getCreatedAt(),
                transaction.getModifiedAt()
        );
    }
}