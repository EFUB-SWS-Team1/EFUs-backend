package com.efus.backend.domain.receipt.dto.response;

public record ReceiptOcrResponse(
        Long transactionId,
        Long receiptId,
        Long recognizedAmount
) {
    public static ReceiptOcrResponse of(
            Long transactionId,
            Long receiptId,
            Long recognizedAmount
    ) {
        return new ReceiptOcrResponse(
                transactionId,
                receiptId,
                recognizedAmount
        );
    }
}