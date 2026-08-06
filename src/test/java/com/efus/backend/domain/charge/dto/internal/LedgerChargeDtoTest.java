package com.efus.backend.domain.charge.dto.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.efus.backend.domain.charge.entity.ChargePaymentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LedgerChargeDtoTest {

    @Test
    void calculatesPartiallyPaidAmountsAndStatus() {
        LedgerChargeDto result = ledgerCharge(30_000L, 20_000L);

        assertThat(result.unpaidAmount()).isEqualTo(10_000L);
        assertThat(result.paymentStatus()).isEqualTo(ChargePaymentStatus.PARTIALLY_PAID);
    }

    @Test
    void calculatesUnpaidStatusWhenNoMemberHasPaid() {
        LedgerChargeDto result = ledgerCharge(30_000L, 0L);

        assertThat(result.unpaidAmount()).isEqualTo(30_000L);
        assertThat(result.paymentStatus()).isEqualTo(ChargePaymentStatus.UNPAID);
    }

    @Test
    void calculatesPaidStatusWhenRequestedAmountIsFullyPaid() {
        LedgerChargeDto result = ledgerCharge(30_000L, 30_000L);

        assertThat(result.unpaidAmount()).isZero();
        assertThat(result.paymentStatus()).isEqualTo(ChargePaymentStatus.PAID);
    }

    private LedgerChargeDto ledgerCharge(long requestedAmount, long paidAmount) {
        return new LedgerChargeDto(1L, "8월 회비", LocalDate.of(2026, 8, 31),
                requestedAmount, paidAmount, null, null, null, false,
                LocalDateTime.of(2026, 8, 1, 12, 0));
    }
}
