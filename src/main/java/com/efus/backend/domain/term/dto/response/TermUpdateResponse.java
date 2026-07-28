package com.efus.backend.domain.term.dto.response;

import com.efus.backend.domain.term.entity.OrganizationTerm;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TermUpdateResponse(
        Long termId,
        Long organizationId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        LocalDateTime modifiedAt
) {
    public static TermUpdateResponse from(OrganizationTerm term) {
        return new TermUpdateResponse(
                term.getId(),
                term.getOrganization().getId(),
                term.getName(),
                term.getStartDate(),
                term.getEndDate(),
                term.getTermStatus().name(),
                term.getModifiedAt()
        );
    }
}