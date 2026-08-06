package com.efus.backend.domain.ledger.dto.response;

import com.efus.backend.domain.charge.dto.internal.LedgerChargeDto;
import com.efus.backend.domain.charge.entity.ChargePaymentStatus;
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
        Long requestedAmount,
        Long paidAmount,
        Long unpaidAmount,
        ChargePaymentStatus paymentStatus,
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
                transaction.getFunding() == null ? null : transaction.getFunding().getId(),
                transaction.getFunding() == null ? null : transaction.getFunding().getName(),
                transaction.isDeleted(),
                receipt != null,
                receipt == null ? null : receipt.getId(),
                // Charge 전용 필드
                null,
                null,
                null,
                null,
                
                transaction.getCreatedAt()
                
        );
    }

     public static LedgerEntryResponse fromCharge(LedgerChargeDto charge) {
         return new LedgerEntryResponse(
                 charge.chargeId(),
                 LedgerEntryType.CHARGE,
                 charge.dueDate(),
                 charge.title(),
                 TransactionType.INCOME,
                 charge.requestedAmount(),
                 charge.fundingId(),
                 charge.fundingName(),
                 charge.deleted(),
                 false,
                 null,
                 
                 // Charge 전용 필드 
                 charge.requestedAmount(),
                 charge.paidAmount(),
                 charge.unpaidAmount(),
                 charge.paymentStatus(),
                 
                 charge.createdAt()
         );
     }
}
