package com.efus.backend.domain.charge.dto.response;

import com.efus.backend.domain.charge.entity.ChargeHistory;
import com.efus.backend.domain.charge.entity.ChargeHistoryActionType;
import java.time.LocalDateTime;

public record ChargeHistoryResponse(
        ChargeHistoryActionType actionType,
        String actorName,
        LocalDateTime changedAt,
        String summary,
        String beforeData,
        String afterData
) {
    public static ChargeHistoryResponse from(ChargeHistory history) {
        return new ChargeHistoryResponse(
                history.getActionType(),
                history.getActorTermMember().getUser().getName(),
                history.getChangedAt(),
                history.getSummary(),
                history.getBeforeData(),
                history.getAfterData()
        );
    }
}
