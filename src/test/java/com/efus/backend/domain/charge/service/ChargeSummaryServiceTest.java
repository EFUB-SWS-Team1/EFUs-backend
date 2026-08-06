package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.dto.internal.LedgerChargeDto;
import com.efus.backend.domain.charge.repository.ChargeRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChargeSummaryServiceTest {
    @Mock
    private ChargeRepository chargeRepository;

    @InjectMocks
    private ChargeSummaryService chargeSummaryService;

    @Test
    void delegatesAllLedgerFiltersToRepository() {
        LocalDate fromDate = LocalDate.of(2026, 8, 1);
        LocalDate toDate = LocalDate.of(2026, 8, 31);
        List<LedgerChargeDto> expected = List.of();
        when(chargeRepository.findLedgerCharges(1L, fromDate, toDate, true, 2L))
                .thenReturn(expected);

        List<LedgerChargeDto> result = chargeSummaryService.getLedgerCharges(
                1L, fromDate, toDate, true, 2L);

        assertThat(result).isSameAs(expected);
        verify(chargeRepository).findLedgerCharges(1L, fromDate, toDate, true, 2L);
    }
}
