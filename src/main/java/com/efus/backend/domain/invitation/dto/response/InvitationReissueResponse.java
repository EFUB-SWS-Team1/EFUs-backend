package com.efus.backend.domain.invitation.dto.response;

import com.efus.backend.domain.invitation.entity.Invitation;
import com.efus.backend.domain.member.entity.TermMemberRole;

import java.time.LocalDateTime;

public record InvitationReissueResponse(
        Long invitationId,
        String code,
        TermMemberRole role,
        LocalDateTime expiresAt
) {
    public static InvitationReissueResponse from(Invitation invitation) {
        return new InvitationReissueResponse(
                invitation.getId(),
                invitation.getCode(),
                invitation.getRole(),
                invitation.getExpiresAt()
        );
    }
}
