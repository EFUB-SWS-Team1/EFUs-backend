package com.efus.backend.domain.transaction.controller;

import com.efus.backend.domain.transaction.dto.response.TransactionHistoryListResponse;
import com.efus.backend.domain.transaction.service.TransactionHistoryService;
import com.efus.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions/{transactionId}/histories")
public class TransactionHistoryController {
    private final TransactionHistoryService transactionHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<TransactionHistoryListResponse>> getTransactionHistories(
            @PathVariable Long transactionId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        TransactionHistoryListResponse response = transactionHistoryService.getTransactionHistories(
                transactionId,
                page,
                size
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}