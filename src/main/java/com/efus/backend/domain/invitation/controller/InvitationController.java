package com.efus.backend.domain.invitation.controller;

import com.efus.backend.domain.invitation.service.InvitationCommandService;
import com.efus.backend.domain.invitation.service.InvitationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.efus.backend.domain.invitation.dto.request.InvitationCodeRequest;
import com.efus.backend.domain.invitation.dto.request.InvitationReissueRequest;
import com.efus.backend.domain.invitation.dto.response.InvitationJoinResponse;
import com.efus.backend.domain.invitation.dto.response.InvitationListResponse;
import com.efus.backend.domain.invitation.dto.response.InvitationReissueResponse;
import com.efus.backend.domain.invitation.dto.response.InvitationValidateResponse;
import com.efus.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationQueryService invitationQueryService;
    private final InvitationCommandService invitationCommandService;


    // GET /api/terms/{termId}/invitations

    @GetMapping("/terms/{termId}/invitations")
    public ResponseEntity<ApiResponse<InvitationListResponse>>
    getInvitations(
            @PathVariable Long termId,
            @RequestHeader("Authorization") String authorization
    ) {
        InvitationListResponse response =
                invitationQueryService.getInvitations(
                        termId,
                        authorization
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }


//      POST /api/terms/{termId}/invitations

    @PostMapping("/terms/{termId}/invitations")
    public ResponseEntity<ApiResponse<InvitationReissueResponse>>
    reissueInvitation(
            @PathVariable Long termId,
            @RequestHeader("Authorization") String authorization,
            @Valid
            @RequestBody InvitationReissueRequest request
    ) {
        InvitationReissueResponse response =
                invitationCommandService.reissueInvitation(
                        termId,
                        authorization,
                        request.getRole()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                response,
                                "초대 코드가 재발급되었습니다."
                        )
                );
    }


//      POST /api/invitations/validate

    @PostMapping("/invitations/validate")
    public ResponseEntity<ApiResponse<InvitationValidateResponse>>
    validateInvitation(
            @RequestHeader("Authorization") String authorization,
            @Valid
            @RequestBody InvitationCodeRequest request
    ) {
        InvitationValidateResponse response =
                invitationQueryService.validateInvitation(
                        authorization,
                        request.getCode()
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }


//      POST /api/invitations/join

    @PostMapping("/invitations/join")
    public ResponseEntity<ApiResponse<InvitationJoinResponse>>
    joinInvitation(
            @RequestHeader("Authorization") String authorization,
            @Valid
            @RequestBody InvitationCodeRequest request
    ) {
        InvitationJoinResponse response =
                invitationCommandService.joinInvitation(
                        authorization,
                        request.getCode()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                response,
                                "기수 가입이 완료되었습니다."
                        )
                );
    }
}