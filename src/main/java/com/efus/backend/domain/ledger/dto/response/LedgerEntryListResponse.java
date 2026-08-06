package com.efus.backend.domain.ledger.dto.response;

import java.util.List;

public record LedgerEntryListResponse(
        Long termId,
        Long totalIncome,
        Long totalExpense,
        Long balance,
        List<LedgerEntryResponse> entries,
        PageInfo pageInfo
) {

    public static LedgerEntryListResponse of(
            Long termId,
            Long totalIncome,
            Long totalExpense,
            List<LedgerEntryResponse> entries,
            int page,
            int size,
            long totalElements
    ) {
        return new LedgerEntryListResponse(
                termId,
                totalIncome,
                totalExpense,
                totalIncome - totalExpense,
                entries,
                PageInfo.of(page, size, totalElements)
        );
    }

    public record PageInfo(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {

        public static PageInfo of(
                int page,
                int size,
                long totalElements
        ) {
            int totalPages = size == 0
                    ? 0
                    : (int) Math.ceil((double) totalElements / size);

            boolean hasNext = page + 1 < totalPages;

            return new PageInfo(
                    page,
                    size,
                    totalElements,
                    totalPages,
                    hasNext
            );
        }
    }
}