package com.efus.backend.domain.ledger.controller;

import com.efus.backend.domain.ledger.dto.response.LedgerEntryListResponse;
import com.efus.backend.domain.ledger.entity.LedgerFlowType;
import com.efus.backend.domain.ledger.service.LedgerService;
import com.efus.backend.global.response.ApiResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/terms/{termId}/ledger-entries")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping
    public ResponseEntity<ApiResponse<LedgerEntryListResponse>> getLedgerEntries(
            @PathVariable Long termId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(defaultValue = "ALL") LedgerFlowType type,
            @RequestParam(defaultValue = "false") boolean includeDeleted,

            // TODO: Transaction-Funding 연관관계 병합 후 실제 필터 적용
            @RequestParam(required = false) Long fundingId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        LedgerEntryListResponse response = ledgerService.getLedgerEntries(
                termId,
                fromDate,
                toDate,
                type,
                includeDeleted,
                fundingId,
                page,
                size
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}