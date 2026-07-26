package com.efus.backend.domain.invitation.dto.response;

import com.efus.backend.domain.invitation.entity.Invitation;
import com.efus.backend.domain.member.entity.TermMemberRole;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record InvitationCodeResponse(
        Long invitationId,
        TermMemberRole role,
        String code,
        OffsetDateTime expiresAt,
        boolean active
) {

    private static final ZoneId KOREA_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    public static InvitationCodeResponse from(
            Invitation invitation
    ) {
        return new InvitationCodeResponse(
                invitation.getId(),
                invitation.getRole(),
                invitation.getCode(),
                invitation.getExpiresAt()
                        .atZone(KOREA_ZONE_ID)
                        .toOffsetDateTime(),
                invitation.isActive()
        );
    }
}