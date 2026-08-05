package com.efus.backend.domain.charge.dto.response;

import java.util.List;

public record ChargePreviewResponse(
        Long totalAmount,
        Long requestedAmount,
        Long remainderAmount,
        int targetCount,
        List<ChargePreviewMemberResponse> members
) {
    public ChargePreviewResponse {
        members = members == null ? List.of() : List.copyOf(members);
    }
}
