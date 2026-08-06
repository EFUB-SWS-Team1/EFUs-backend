package com.efus.backend.domain.funding.controller;

import com.efus.backend.domain.funding.dto.request.FundingCreateRequest;
import com.efus.backend.domain.funding.dto.response.FundingListResponse;
import com.efus.backend.domain.funding.dto.response.FundingResponse;
import com.efus.backend.domain.funding.dto.response.FundingSummaryResponse;
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

    // [행사 목록 조회]
    @GetMapping
    public ResponseEntity<ApiResponse<FundingListResponse>> getFundingList(
            @PathVariable Long termId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        FundingListResponse response = fundingService.getFundingList(termId, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // [행사 예산 요약 조회]
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<FundingSummaryResponse>> getFundingSummary(
            @PathVariable Long termId
    ) {
        FundingSummaryResponse response = fundingService.getFundingSummary(termId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}