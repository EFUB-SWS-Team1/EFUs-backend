package com.efus.backend.domain.invitation.dto.response;

import com.efus.backend.domain.invitation.entity.Invitation;
import com.efus.backend.domain.member.entity.TermMemberRole;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record InvitationReissueResponse(
        Long invitationId,
        Long termId,
        TermMemberRole role,
        String code,
        OffsetDateTime issuedAt,
        OffsetDateTime expiresAt,
        boolean active
) {

    private static final ZoneId KOREA_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    public static InvitationReissueResponse from(
            Long termId,
            Invitation invitation
    ) {
        return new InvitationReissueResponse(
                invitation.getId(),
                termId,
                invitation.getRole(),
                invitation.getCode(),
                invitation.getCreatedAt()
                        .atZone(KOREA_ZONE_ID)
                        .toOffsetDateTime(),
                invitation.getExpiresAt()
                        .atZone(KOREA_ZONE_ID)
                        .toOffsetDateTime(),
                invitation.isActive()
        );
    }
}