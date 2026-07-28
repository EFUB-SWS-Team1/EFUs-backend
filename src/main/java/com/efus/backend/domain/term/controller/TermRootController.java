package com.efus.backend.domain.term.controller;

import com.efus.backend.domain.term.dto.request.TermUpdateRequest;
import com.efus.backend.domain.term.dto.response.TermDetailResponse;
import com.efus.backend.domain.term.dto.response.TermUpdateResponse;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.term.service.TermService;
import com.efus.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TermRootController { // URI 최상단이 /api/terms 인 경우를 처리할 컨트롤러

    private final TermQueryService termQueryService;
    private final TermService termService;

    // 기수 상세 조회[cite: 6]
    @GetMapping("/api/terms/{termId}")
    public ResponseEntity<ApiResponse<TermDetailResponse>> getTermDetail(
            @PathVariable("termId") Long termId) { // 조회할 기수 ID[cite: 6]

        TermDetailResponse response = termQueryService.getTermDetail(termId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/api/terms/{termId}")
    public ResponseEntity<ApiResponse<TermUpdateResponse>> updateTerm(
            @PathVariable("termId") Long termId, // 수정할 기수 ID[cite: 7]
            @RequestBody TermUpdateRequest request) {

        TermUpdateResponse response = termService.updateTerm(termId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}