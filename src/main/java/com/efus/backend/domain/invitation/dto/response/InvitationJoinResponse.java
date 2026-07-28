package com.efus.backend.domain.invitation.dto.response;

import com.efus.backend.domain.member.entity.TermMemberRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record InvitationJoinResponse(
        Long termMemberId,
        OrganizationInfo organization,
        TermInfo term,
        TermMemberRole role,
        OffsetDateTime joinedAt
) {

    private static final ZoneId KOREA_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    public static InvitationJoinResponse of(
            Long termMemberId,
            Long organizationId,
            String organizationName,
            Long termId,
            String termName,
            LocalDate startDate,
            LocalDate endDate,
            String termStatus,
            TermMemberRole role,
            LocalDateTime joinedAt
    ) {
        return new InvitationJoinResponse(
                termMemberId,
                new OrganizationInfo(
                        organizationId,
                        organizationName
                ),
                new TermInfo(
                        termId,
                        termName,
                        startDate,
                        endDate,
                        termStatus
                ),
                role,
                joinedAt.atZone(KOREA_ZONE_ID)
                        .toOffsetDateTime()
        );
    }

    public record OrganizationInfo(
            Long organizationId,
            String name
    ) {
    }

    public record TermInfo(
            Long termId,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            String status
    ) {
    }
}
