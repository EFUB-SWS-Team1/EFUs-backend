package com.efus.backend.domain.receipt.dto.response;

import com.efus.backend.domain.receipt.entity.Receipt;
import java.time.LocalDateTime;

public record ReceiptResponse(
        Long receiptId,
        Long transactionId,
        Long uploadedByTermMemberId,
        String uploadedByMemberName,
        String storageKey,
        String presignedUrl,
        String originalFilename,
        String contentType,
        Long fileSize,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReceiptResponse from(Receipt receipt) {
        return new ReceiptResponse(
                receipt.getId(),
                receipt.getTransaction().getId(),
                receipt.getUploadedByTermMember().getId(),
                receipt.getUploadedByTermMember().getUser().getName(),
                receipt.getStorageKey(),
                receipt.getPresignedUrl(),
                receipt.getOriginalFilename(),
                receipt.getContentType(),
                receipt.getFileSize(),
                receipt.getCreatedAt(),
                receipt.getModifiedAt()
        );
    }
}