package com.efus.backend.domain.term.dto.response;

import java.time.LocalDate;
import java.util.List;

public record TermListResponse(
        Long organizationId,
        String organizationName,
        List<TermDetailDto> terms
) {
    public record TermDetailDto(
            Long termId,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            Long memberCount,
            String myRole
    ) {}
}