package com.efus.backend.domain.organization.dto.response;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.entity.OrganizationTerm;

import java.time.LocalDate;

public record OrganizationResponse(
        Long organizationId,
        String name,
        ActiveTermDto activeTerm,
        String myRole
) {
    public record ActiveTermDto(
            Long termId,
            String name,
            LocalDate startDate,
            String status,
            Long memberCount
    ) {}

    public static OrganizationResponse of(Organization org, OrganizationTerm term, TermMember termMember, Long memberCount) {
        ActiveTermDto activeTermDto = new ActiveTermDto(
                term.getId(),
                term.getName(),
                term.getStartDate(),
                term.getTermStatus().name(),
                memberCount
        );

        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                activeTermDto,
                termMember.getRole().name()
        );
    }

    // 활성 기수가 없을 때
    public static OrganizationResponse ofInactive(Organization org) {
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                null,
                null
        );
    }
}