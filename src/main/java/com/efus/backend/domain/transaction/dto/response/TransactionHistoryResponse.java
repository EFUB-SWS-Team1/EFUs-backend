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
            history.getActorTermMember().getId(),
            history.getActorTermMember().getUser().getName(),
            history.getActionType(),
            history.getSummary(),
            history.getBeforeData(),
            history.getAfterData(),
            history.getChangedAt()
    );
}
}