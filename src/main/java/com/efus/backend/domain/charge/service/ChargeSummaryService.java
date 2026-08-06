package com.efus.backend.domain.charge.service;

import com.efus.backend.domain.charge.dto.internal.LedgerChargeDto;
import com.efus.backend.domain.charge.repository.ChargeRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargeSummaryService {
    private final ChargeRepository chargeRepository;

    public List<LedgerChargeDto> getLedgerCharges(Long termId, LocalDate fromDate,
                                                   LocalDate toDate, boolean includeDeleted,
                                                   Long fundingId) {
        return chargeRepository.findLedgerCharges(
                termId, fromDate, toDate, includeDeleted, fundingId);
    }
}
