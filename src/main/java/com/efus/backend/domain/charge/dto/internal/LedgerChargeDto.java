package com.efus.backend.domain.charge.dto.internal;

import com.efus.backend.domain.charge.entity.ChargePaymentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LedgerChargeDto(
        Long chargeId,
        String title,
        LocalDate dueDate,
        Long requestedAmount,
        Long paidAmount,
        Long unpaidAmount,
        ChargePaymentStatus paymentStatus,
        Long fundingId,
        String fundingName,
        String memo,
        boolean deleted,
        LocalDateTime createdAt
) {
    public LedgerChargeDto(Long chargeId, String title, LocalDate dueDate, Long requestedAmount,
                           Long paidAmount, Long fundingId, String fundingName, String memo,
                           boolean deleted, LocalDateTime createdAt) {
        this(chargeId, title, dueDate, requestedAmount, paidAmount,
                requestedAmount - paidAmount,
                calculatePaymentStatus(requestedAmount, paidAmount),
                fundingId, fundingName, memo, deleted, createdAt);
    }

    private static ChargePaymentStatus calculatePaymentStatus(long requestedAmount, long paidAmount) {
        if (paidAmount == 0L) {
            return ChargePaymentStatus.UNPAID;
        }
        if (paidAmount == requestedAmount) {
            return ChargePaymentStatus.PAID;
        }
        return ChargePaymentStatus.PARTIALLY_PAID;
    }
}
