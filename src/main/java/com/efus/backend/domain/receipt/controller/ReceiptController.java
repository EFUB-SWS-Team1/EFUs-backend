package com.efus.backend.domain.receipt.controller;

import com.efus.backend.domain.receipt.dto.response.ReceiptResponse;
import com.efus.backend.domain.receipt.service.ReceiptService;
import com.efus.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.efus.backend.domain.receipt.dto.request.ReceiptRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions/{transactionId}/receipt")
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping
    public ResponseEntity<ApiResponse<ReceiptResponse>> getReceipt(
            @PathVariable Long transactionId
    ) {
        ReceiptResponse response = receiptService.getReceipt(transactionId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/presigned-url")
    public ResponseEntity<ApiResponse<ReceiptResponse>> createOrReplaceReceiptUploadUrl(
            @PathVariable Long transactionId,
            @Valid @RequestBody ReceiptRequest request
    ) {
        ReceiptResponse response = receiptService.createOrReplaceReceiptUploadUrl(transactionId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "영수증 업로드 URL이 생성되었습니다."));
    }
}