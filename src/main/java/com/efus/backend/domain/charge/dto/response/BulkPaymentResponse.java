package com.efus.backend.domain.charge.dto.response;

import java.time.LocalDateTime;

public record BulkPaymentResponse(Long chargeId, int processedCount, LocalDateTime paidAt) {
}
