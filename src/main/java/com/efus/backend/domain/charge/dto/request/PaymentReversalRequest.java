package com.efus.backend.domain.charge.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PaymentReversalRequest(@NotBlank String reason) {
}
