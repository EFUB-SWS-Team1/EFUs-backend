package com.efus.backend.domain.ledger.dto.response;

import com.efus.backend.domain.ledger.entity.LedgerEntryType;
import com.efus.backend.domain.receipt.entity.Receipt;
import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.entity.TransactionType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record LedgerEntryResponse(
        Long entryId,
        LedgerEntryType entryType,
        LocalDate date,
        String title,
        TransactionType transactionType,
        Long amount,
        Long fundingId,
        String fundingName,
        boolean deleted,
        boolean hasReceipt,
        Long receiptId,
        LocalDateTime createdAt
) {

    public static LedgerEntryResponse fromTransaction(
            Transaction transaction,
            Map<Long, Receipt> receiptMap
    ) {
        Receipt receipt = receiptMap.get(transaction.getId());

        return new LedgerEntryResponse(
                transaction.getId(),
                LedgerEntryType.TRANSACTION,
                transaction.getTransactionDate(),
                transaction.getTitle(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                // TODO: Transaction-Funding 연관관계 병합 후 fundingId 응답 연결
                // transaction.getFunding() == null ? null : transaction.getFunding().getId(),
                null,
                // TODO: Transaction-Funding 연관관계 병합 후 fundingName 응답 연결
                // transaction.getFunding() == null ? null : transaction.getFunding().getName(),
                null,
                transaction.isDeleted(),
                receipt != null,
                receipt == null ? null : receipt.getId(),
                transaction.getCreatedAt()
        );
    }

    // TODO: Charge 도메인 병합 후 주석 해제
    // public static LedgerEntryResponse fromCharge(Charge charge) {
    //     return new LedgerEntryResponse(
    //             charge.getId(),
    //             LedgerEntryType.CHARGE,
    //             charge.getDueDate(),
    //             charge.getTitle(),
    //             TransactionType.INCOME,
    //             charge.getAmount(),
    //             null,
    //             null,
    //             charge.isDeleted(),
    //             false,
    //             null,
    //             charge.getCreatedAt()
    //     );
    // }
}