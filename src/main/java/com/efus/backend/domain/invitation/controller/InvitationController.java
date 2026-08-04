package com.efus.backend.domain.invitation.controller;

import com.efus.backend.domain.invitation.dto.response.InvitationListResponse;
import com.efus.backend.domain.invitation.service.InvitationQueryService;
import com.efus.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationQueryService invitationQueryService;

    @GetMapping("/terms/{termId}/invitations")
    public ResponseEntity<ApiResponse<InvitationListResponse>> getInvitations(
            @PathVariable Long termId
    ) {
        InvitationListResponse response = invitationQueryService.getInvitations(termId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
