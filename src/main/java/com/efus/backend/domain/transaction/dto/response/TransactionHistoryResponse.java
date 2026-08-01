package com.efus.backend.domain.transaction.dto.response;

import com.efus.backend.domain.transaction.entity.TransactionActionType;
import com.efus.backend.domain.transaction.entity.TransactionHistory;
import java.time.LocalDateTime;

public record TransactionHistoryResponse(
        Long historyId,
        Long transactionId,
        Long actorTermMemberId,
        String actorName,
        TransactionActionType actionType,
        String summary,
        String beforeData,
        String afterData,
        LocalDateTime changedAt
) {
    public static TransactionHistoryResponse from(TransactionHistory history) {
    return new TransactionHistoryResponse(
            history.getId(),
            history.getTransaction().getId(),
            null, // TODO: member 연관관계 병합 후 actorTermMemberId 연결
            null, // TODO: member/user 연관관계 병합 후 actorName 연결
            history.getActionType(),
            history.getSummary(),
            history.getBeforeData(),
            history.getAfterData(),
            history.getChangedAt()
    );
}
}