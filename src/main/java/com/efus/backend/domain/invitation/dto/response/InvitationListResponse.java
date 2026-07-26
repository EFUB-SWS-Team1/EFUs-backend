package com.efus.backend.domain.invitation.dto.response;

import java.util.List;

public record InvitationListResponse(
        InvitationTermResponse term,
        List<InvitationCodeResponse> invitations
) {

    public InvitationListResponse {
        invitations = invitations == null
                ? List.of()
                : List.copyOf(invitations);
    }

    public static InvitationListResponse of(
            InvitationTermResponse term,
            List<InvitationCodeResponse> invitations
    ) {
        return new InvitationListResponse(
                term,
                invitations
        );
    }
}