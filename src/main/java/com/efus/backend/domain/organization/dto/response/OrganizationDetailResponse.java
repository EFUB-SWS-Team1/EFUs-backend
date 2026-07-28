package com.efus.backend.domain.organization.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrganizationDetailResponse(
        Long organizationId,
        String name,
        LocalDateTime createdAt,
        ActiveTermDetailDto activeTerm,
        ClosedTermDto recentClosedTerm
) {
    public record ActiveTermDetailDto(
            Long termId,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            Long memberCount,
            String myRole
    ) {}

    public record ClosedTermDto(
            Long termId,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            String status
    ) {}
}