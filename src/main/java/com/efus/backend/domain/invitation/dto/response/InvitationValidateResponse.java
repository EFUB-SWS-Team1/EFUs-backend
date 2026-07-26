package com.efus.backend.domain.invitation.dto.response;

import com.efus.backend.domain.member.entity.TermMemberRole;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record InvitationValidateResponse(
        boolean valid,
        OrganizationInfo organization,
        TermInfo term,
        TermMemberRole role,
        OffsetDateTime expiresAt
) {

    private static final ZoneId KOREA_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    public static InvitationValidateResponse of(
            Long organizationId,
            String organizationName,
            Long termId,
            String termName,
            String termStatus,
            TermMemberRole role,
            LocalDateTime expiresAt
    ) {
        return new InvitationValidateResponse(
                true,
                new OrganizationInfo(
                        organizationId,
                        organizationName
                ),
                new TermInfo(
                        termId,
                        termName,
                        termStatus
                ),
                role,
                expiresAt.atZone(KOREA_ZONE_ID)
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
            String status
    ) {
    }
}
