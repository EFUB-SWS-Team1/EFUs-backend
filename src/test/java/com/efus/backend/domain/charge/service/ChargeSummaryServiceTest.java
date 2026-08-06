package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.dto.internal.LedgerChargeDto;
import com.efus.backend.domain.charge.entity.ChargePaymentStatus;
import com.efus.backend.domain.charge.repository.ChargeMemberRepository;
import com.efus.backend.domain.charge.repository.ChargeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChargeSummaryServiceTest {

    @Test
    void ledgerChargeCalculatesUnpaidAmountAndPaymentStatus() {
        LocalDate dueDate = LocalDate.of(2026, 8, 31);
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 1, 12, 0);

        LedgerChargeDto unpaid = dto(1L, 30_000L, 0L, dueDate, createdAt);
        LedgerChargeDto partiallyPaid = dto(2L, 30_000L, 20_000L, dueDate, createdAt);
        LedgerChargeDto paid = dto(3L, 30_000L, 30_000L, dueDate, createdAt);

        assertThat(unpaid.unpaidAmount()).isEqualTo(30_000L);
        assertThat(unpaid.paymentStatus()).isEqualTo(ChargePaymentStatus.UNPAID);
        assertThat(partiallyPaid.unpaidAmount()).isEqualTo(10_000L);
        assertThat(partiallyPaid.paymentStatus()).isEqualTo(ChargePaymentStatus.PARTIALLY_PAID);
        assertThat(paid.unpaidAmount()).isZero();
        assertThat(paid.paymentStatus()).isEqualTo(ChargePaymentStatus.PAID);
    }

    @Test
    void getLedgerChargesDelegatesAllFiltersToSingleRepositoryQuery() {
        ChargeMemberRepository memberRepository = mock(ChargeMemberRepository.class);
        ChargeRepository chargeRepository = mock(ChargeRepository.class);
        ChargeSummaryService service = new ChargeSummaryService(memberRepository, chargeRepository);
        LocalDate fromDate = LocalDate.of(2026, 8, 1);
        LocalDate toDate = LocalDate.of(2026, 8, 31);
        List<LedgerChargeDto> expected = List.of(dto(
                1L, 30_000L, 20_000L, toDate, LocalDateTime.of(2026, 8, 1, 12, 0)));
        when(chargeRepository.findLedgerCharges(10L, fromDate, toDate, true, 20L))
                .thenReturn(expected);

        List<LedgerChargeDto> actual = service.getLedgerCharges(
                10L, fromDate, toDate, true, 20L);

        assertThat(actual).isSameAs(expected);
        verify(chargeRepository).findLedgerCharges(10L, fromDate, toDate, true, 20L);
    }

    private LedgerChargeDto dto(
            Long id, Long requestedAmount, Long paidAmount,
            LocalDate dueDate, LocalDateTime createdAt
    ) {
        return new LedgerChargeDto(
                id, "charge", dueDate, requestedAmount, paidAmount,
                null, null, null, false, createdAt);
    }
}
