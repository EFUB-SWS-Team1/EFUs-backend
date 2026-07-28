package com.efus.backend.domain.invitation.dto.response;

public record InvitationTermResponse(
        Long termId,
        Long organizationId,
        String organizationName,
        String termName,
        String status
) {
}