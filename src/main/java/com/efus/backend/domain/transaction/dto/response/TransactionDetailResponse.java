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
    // TODO: term,funding,member 연관관계 병합 후 주석 해제
//    public static TransactionDetailResponse from(Transaction transaction) {
//        return new TransactionDetailResponse(
//                transaction.getId(),
//                transaction.getTerm().getId(),
//                transaction.getFunding() == null ? null : transaction.getFunding().getId(),
//                null, // TODO: Funding Entity 확정 후 fundingName 연결
//                transaction.getTransactionType(),
//                transaction.getTitle(),
//                transaction.getAmount(),
//                transaction.getTransactionDate(),
//                transaction.getMemo(),
//                transaction.getCreatedByTermMember().getId(),
//                null, // TODO: TermMember/User Entity 확정 후 createdByMemberName 연결
//                transaction.getUpdatedByTermMember() == null ? null : transaction.getUpdatedByTermMember().getId(),
//                null, // TODO: TermMember/User Entity 확정 후 updatedByMemberName 연결
//                transaction.getCreatedAt(),
//                transaction.getModifiedAt()
//        );
//    }
}