package com.efus.backend.domain.funding.controller;

import com.efus.backend.domain.funding.dto.request.FundingCreateRequest;
import com.efus.backend.domain.funding.dto.response.FundingResponse;
import com.efus.backend.domain.funding.service.FundingService;
import com.efus.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.efus.backend.domain.funding.dto.response.FundingDetailResponse;

@RestController
@RequestMapping("/api/terms/{termId}/fundings")
@RequiredArgsConstructor
public class FundingController {

    private final FundingService fundingService;

    @PostMapping
    public ResponseEntity<ApiResponse<FundingResponse>> createFunding(
            @PathVariable Long termId,
            @Valid @RequestBody FundingCreateRequest request
    ) {
        FundingResponse response = fundingService.createFunding(termId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "행사가 등록되었습니다."));
    }

    @GetMapping("/{fundingId}")
    public ResponseEntity<ApiResponse<FundingDetailResponse>> getFundingDetail(
            @PathVariable Long termId,
            @PathVariable Long fundingId
    ) {
        FundingDetailResponse response = fundingService.getFundingDetail(termId, fundingId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}