package com.efus.backend.domain.term.controller;

import com.efus.backend.domain.term.dto.request.TermCreateRequest;
import com.efus.backend.domain.term.dto.response.TermCreateResponse;
import com.efus.backend.domain.term.dto.response.TermListResponse;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.term.service.TermService;
import com.efus.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations/{organizationId}/terms")
@RequiredArgsConstructor
public class TermController {
    private final TermQueryService termQueryService;
    private final TermService termService;

    @PostMapping
    public ResponseEntity<ApiResponse<TermCreateResponse>> createTerm(
            @PathVariable("organizationId") Long organizationId,
            @Valid @RequestBody TermCreateRequest request) {

        TermCreateResponse response = termService.createTerm(organizationId, request);

        // HTTP Status code: 201 Created 반환[cite: 4]
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

//    @GetMapping
//    public ResponseEntity<ApiResponse<TermListResponse>> getTermList(
//            @PathVariable("organizationId") Long organizationId) { // 기수 목록을 조회할 단체 ID[cite: 5]
//
//        TermListResponse response = termQueryService.getTermList(organizationId);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
}