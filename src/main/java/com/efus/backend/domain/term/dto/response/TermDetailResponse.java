package com.efus.backend.domain.term.dto.response;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.entity.OrganizationTerm;

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

    public static TermDetailResponse of(OrganizationTerm term, Organization org, Long memberCount, TermMember member) {

        OrganizationDto organizationDto = new OrganizationDto(
                org.getId(),
                org.getName()
        );

        // 조회하는 유저가 이 기수의 멤버가 아닐 경우 null 처리
        Long memberId = (member != null) ? member.getId() : null;
        String roleName = (member != null) ? member.getRole().name() : null;

        return new TermDetailResponse(
                term.getId(),
                organizationDto,
                term.getName(),
                term.getStartDate(),
                term.getEndDate(),
                term.getTermStatus().name(),
                memberCount,
                memberId,
                roleName,
                term.getCreatedAt(),
                term.getModifiedAt()
        );
    }
}