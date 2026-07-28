package com.efus.backend.domain.term.dto.response;

import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TermCreateResponse(
        Long termId,
        Long organizationId,
        String organizationName,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Long myTermMemberId,
        String myRole,
        LocalDateTime createdAt
) {
    public static TermCreateResponse of(OrganizationTerm term, Organization org, Long myTermMemberId, String myRole) {
        return new TermCreateResponse(
                term.getId(),
                org.getId(),
                org.getName(),
                term.getName(),
                term.getStartDate(),
                term.getEndDate(),
                term.getTermStatus().name(),
                myTermMemberId,
                myRole,
                term.getCreatedAt()
        );
    }
}