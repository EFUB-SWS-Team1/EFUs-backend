package com.efus.backend.domain.term.controller;

import com.efus.backend.domain.term.dto.request.TermCloseRequest;
import com.efus.backend.domain.term.dto.request.TermUpdateRequest;
import com.efus.backend.domain.term.dto.response.TermCloseResponse;
import com.efus.backend.domain.term.dto.response.TermDetailResponse;
import com.efus.backend.domain.term.dto.response.TermUpdateResponse;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.term.service.TermService;
import com.efus.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/terms/{termId}")
public class TermRootController {

    private final TermQueryService termQueryService;
    private final TermService termService;

    // [기수 상세 조회]
    @GetMapping
    public ResponseEntity<ApiResponse<TermDetailResponse>> getTermDetail(
            @PathVariable("termId") Long termId) {

        TermDetailResponse response = termQueryService.getTermDetail(termId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // [기수 정보 수정]
    @PatchMapping
    public ResponseEntity<ApiResponse<TermUpdateResponse>> updateTerm(
            @PathVariable("termId") Long termId,
            @RequestBody TermUpdateRequest request) {

        TermUpdateResponse response = termService.updateTerm(termId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/close")
    public ResponseEntity<ApiResponse<TermCloseResponse>> closeTerm(
            @PathVariable("termId") Long termId,
            @Valid @RequestBody TermCloseRequest request) {

        TermCloseResponse response = termService.closeTerm(termId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}