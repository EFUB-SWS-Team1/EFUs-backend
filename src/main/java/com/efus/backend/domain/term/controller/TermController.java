package com.efus.backend.domain.term.controller;

import com.efus.backend.domain.term.dto.request.TermCreateRequest;
import com.efus.backend.domain.term.dto.response.TermCreateResponse;
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
}