package com.efus.backend.domain.transaction.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record TransactionHistoryListResponse(
        List<TransactionHistoryResponse> histories,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static TransactionHistoryListResponse from(Page<TransactionHistoryResponse> historyPage) {
        return new TransactionHistoryListResponse(
                historyPage.getContent(),
                historyPage.getNumber(),
                historyPage.getSize(),
                historyPage.getTotalElements(),
                historyPage.getTotalPages(),
                historyPage.hasNext()
        );
    }
}