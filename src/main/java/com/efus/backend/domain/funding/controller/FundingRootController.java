package com.efus.backend.domain.funding.controller;

import com.efus.backend.domain.funding.dto.request.FundingUpdateRequest;
import com.efus.backend.domain.funding.dto.response.FundingResponse;
import com.efus.backend.domain.funding.service.FundingService;
import com.efus.backend.domain.ledger.dto.response.LedgerEntryListResponse;
import com.efus.backend.domain.ledger.entity.LedgerFlowType;
import com.efus.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fundings")
@RequiredArgsConstructor
public class FundingRootController {

    private final FundingService fundingService;

    @PatchMapping("/{fundingId}")
    public ResponseEntity<ApiResponse<FundingResponse>> updateFunding(
            @PathVariable Long fundingId,
            @Valid @RequestBody FundingUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                fundingService.updateFunding(fundingId, request),
                "행사가 수정되었습니다."
        ));
    }

    @GetMapping("/{fundingId}/ledger-entries")
    public ResponseEntity<ApiResponse<LedgerEntryListResponse>> getLedgerEntries(
            @PathVariable Long fundingId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "ALL") LedgerFlowType type,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(fundingService.getLedgerEntries(
                fundingId, fromDate, toDate, type, includeDeleted, page, size)));
    }
}
