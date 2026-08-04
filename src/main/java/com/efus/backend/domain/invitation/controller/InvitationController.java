package com.efus.backend.domain.invitation.controller;

import com.efus.backend.domain.invitation.dto.request.InvitationCodeRequest;
import com.efus.backend.domain.invitation.dto.request.InvitationReissueRequest;
import com.efus.backend.domain.invitation.dto.response.InvitationListResponse;
import com.efus.backend.domain.invitation.dto.response.InvitationJoinResponse;
import com.efus.backend.domain.invitation.dto.response.InvitationReissueResponse;
import com.efus.backend.domain.invitation.dto.response.InvitationValidateResponse;
import com.efus.backend.domain.invitation.service.InvitationCommandService;
import com.efus.backend.domain.invitation.service.InvitationQueryService;
import com.efus.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationQueryService invitationQueryService;
    private final InvitationCommandService invitationCommandService;

    @PostMapping("/invitations/validate")
    public ResponseEntity<ApiResponse<InvitationValidateResponse>> validateInvitation(
            @RequestBody InvitationCodeRequest request
    ) {
        InvitationValidateResponse response =
                invitationQueryService.validateInvitation(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/invitations/join")
    public ResponseEntity<ApiResponse<InvitationJoinResponse>> join(
            @Valid @RequestBody InvitationCodeRequest request
    ) {
        InvitationJoinResponse response = invitationCommandService.join(request);

        return ResponseEntity.ok(ApiResponse.success(
                response,
                "기수 가입이 완료되었습니다."
        ));
    }

    @GetMapping("/terms/{termId}/invitations")
    public ResponseEntity<ApiResponse<InvitationListResponse>> getInvitations(
            @PathVariable Long termId
    ) {
        InvitationListResponse response = invitationQueryService.getInvitations(termId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/terms/{termId}/invitations")
    public ResponseEntity<ApiResponse<InvitationReissueResponse>> reissueInvitation(
            @PathVariable Long termId,
            @Valid @RequestBody InvitationReissueRequest request
    ) {
        InvitationReissueResponse response =
                invitationCommandService.reissueInvitation(termId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
