package com.efus.backend.domain.transaction.dto.internal;

import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.entity.TransactionType;
import java.time.LocalDate;

public record TransactionSnapshot(
        TransactionType transactionType,
        Long fundingId,
        String title,
        Long amount,
        LocalDate transactionDate,
        String memo
) {

    public static TransactionSnapshot from(Transaction transaction) {
        return new TransactionSnapshot(
                transaction.getTransactionType(),
                transaction.getFunding() == null ? null : transaction.getFunding().getId(),
                transaction.getTitle(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getMemo()
        );
    }
}