package com.efus.backend.domain.charge.dto.response;

public record ChargeCreateResponse(Long chargeId, Long requestedAmount, int targetCount) {
}
