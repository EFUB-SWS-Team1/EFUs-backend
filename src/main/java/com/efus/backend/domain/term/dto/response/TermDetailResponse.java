package com.efus.backend.domain.term.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TermDetailResponse(
        Long termId,
        OrganizationDto organization,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Long memberCount,
        Long myTermMemberId,
        String myRole,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public record OrganizationDto(
            Long organizationId,
            String name
    ) {}
}