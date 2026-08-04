package com.efus.backend.domain.member.controller;

import com.efus.backend.domain.member.dto.request.TermMemberListRequest;
import com.efus.backend.domain.member.dto.response.TermMemberListResponse;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/terms/{termId}/members")
public class MemberController {

    private final MemberQueryService memberQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<TermMemberListResponse>> getTermMembers(
            @PathVariable Long termId,
            @Valid @ModelAttribute TermMemberListRequest request
    ) {
        TermMemberListResponse response = memberQueryService.getTermMembers(termId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
