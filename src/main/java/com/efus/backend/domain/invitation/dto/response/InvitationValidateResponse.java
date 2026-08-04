package com.efus.backend.domain.invitation.dto.response;

import com.efus.backend.domain.invitation.entity.Invitation;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.term.entity.OrganizationTerm;

import java.time.LocalDateTime;

public record InvitationValidateResponse(
        boolean valid,
        String organizationName,
        String termName,
        TermMemberRole role,
        LocalDateTime expiresAt
) {
    public static InvitationValidateResponse from(
            Invitation invitation,
            OrganizationTerm term
    ) {
        return new InvitationValidateResponse(
                true,
                term.getOrganization().getName(),
                term.getName(),
                invitation.getRole(),
                invitation.getExpiresAt()
        );
    }
}
