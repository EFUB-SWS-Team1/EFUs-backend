package com.efus.backend.domain.charge.dto.internal;

public record MemberChargeSummary(
        Long paidAmount,
        Long unpaidAmount
) {
}
