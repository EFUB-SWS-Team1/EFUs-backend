package com.efus.backend.domain.charge.dto.request;

import java.time.LocalDateTime;

public record ChargePaymentRequest(LocalDateTime paidAt) {
}
