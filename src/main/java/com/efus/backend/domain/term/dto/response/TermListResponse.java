package com.efus.backend.domain.term.dto.response;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.entity.OrganizationTerm;

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
    ) {
        public static TermDetailDto of(OrganizationTerm term, Long memberCount, TermMember member) {

            String roleName = (member != null) ? member.getRole().name() : null;

            return new TermDetailDto(
                    term.getId(),
                    term.getName(),
                    term.getStartDate(),
                    term.getEndDate(),
                    term.getTermStatus().name(),
                    memberCount,
                    roleName
            );
        }
    }

    public static TermListResponse of(Organization org, List<TermDetailDto> termDtos) {
        return new TermListResponse(
                org.getId(),
                org.getName(),
                termDtos
        );
    }
}