package com.efus.backend.domain.charge.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record ChargeHistoryListResponse(
        List<ChargeHistoryResponse> histories,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static ChargeHistoryListResponse from(Page<ChargeHistoryResponse> historyPage) {
        return new ChargeHistoryListResponse(
                historyPage.getContent(),
                historyPage.getNumber(),
                historyPage.getSize(),
                historyPage.getTotalElements(),
                historyPage.getTotalPages(),
                historyPage.hasNext()
        );
    }
}
