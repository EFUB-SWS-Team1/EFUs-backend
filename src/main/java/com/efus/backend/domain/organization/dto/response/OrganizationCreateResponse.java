package com.efus.backend.domain.organization.dto.response;

import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.member.entity.TermMember;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrganizationCreateResponse(
        Long organizationId,
        String name,
        LocalDateTime createdAt,
        InitialTermDto initialTerm,
        Long myTermMemberId,
        String myRole
) {
    public record InitialTermDto(
            Long termId,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            String status
    ) {}

    public static OrganizationCreateResponse of(Organization org, OrganizationTerm term, TermMember termMember) {
        InitialTermDto termDto = new InitialTermDto(
                term.getId(),
                term.getName(),
                term.getStartDate(),
                term.getEndDate(),
                term.getTermStatus().name()
        );

        return new OrganizationCreateResponse(
                org.getId(),
                org.getName(),
                org.getCreatedAt(),
                termDto,
                termMember.getId(),
                termMember.getRole().name()
        );
    }
}