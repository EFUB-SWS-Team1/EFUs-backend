package com.efus.backend.domain.organization.dto.response;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.entity.OrganizationTerm;

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
    ) {
    }

    public record ClosedTermDto(
            Long termId,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            String status
    ) {
    }

    public static OrganizationDetailResponse of(Organization org, OrganizationTerm activeTerm, OrganizationTerm recentClosedTerm, Long memberCount, TermMember member) {

        // 현재 기수가 null이 아닐 때만 dto 조립
        ActiveTermDetailDto activeTermDto = null;
        if (activeTerm != null) {
            String roleName = (member != null) ? member.getRole().name() : null;

            activeTermDto = new ActiveTermDetailDto(
                    activeTerm.getId(),
                    activeTerm.getName(),
                    activeTerm.getStartDate(),
                    null,
                    "ACTIVE",
                    memberCount,
                    roleName
            );
        }

        // 최근 종료 기수가 null이 아닐 때만 dto 조립
        ClosedTermDto closedTermDto = null;
        if (recentClosedTerm != null) {
            closedTermDto = new ClosedTermDto(
                    recentClosedTerm.getId(),
                    recentClosedTerm.getName(),
                    recentClosedTerm.getStartDate(),
                    recentClosedTerm.getEndDate(),
                    "CLOSED"
            );
        }

        return new OrganizationDetailResponse(
                org.getId(),
                org.getName(),
                org.getCreatedAt(),
                activeTermDto,
                closedTermDto
        );
    }
}