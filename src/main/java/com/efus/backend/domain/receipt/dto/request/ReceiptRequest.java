package com.efus.backend.domain.receipt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReceiptRequest(
        @NotBlank
        String originalFilename,

        @NotBlank
        String contentType,

        @NotNull
        @Positive
        Long fileSize
) {
}