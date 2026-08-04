package com.efus.backend.domain.ledger.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

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
            Page<?> page
    ) {
        return new LedgerEntryListResponse(
                termId,
                totalIncome,
                totalExpense,
                totalIncome - totalExpense,
                entries,
                PageInfo.from(page)
        );
    }

    public record PageInfo(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {

        public static PageInfo from(Page<?> page) {
            return new PageInfo(
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.hasNext()
            );
        }
    }
}