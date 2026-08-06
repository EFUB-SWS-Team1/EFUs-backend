package com.efus.backend.domain.charge.service;

import com.efus.backend.domain.charge.dto.internal.MemberChargeSummary;
import com.efus.backend.domain.charge.dto.internal.LedgerChargeDto;
import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import com.efus.backend.domain.charge.repository.ChargeMemberRepository;
import com.efus.backend.domain.charge.repository.ChargeRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargeSummaryService {

    private final ChargeMemberRepository chargeMemberRepository;
    private final ChargeRepository chargeRepository;

    public MemberChargeSummary calculateMemberChargeSummary(Long termId, Long termMemberId) {
        return chargeMemberRepository.summarizeAmountsByTermMember(termId, termMemberId);
    }

    public Page<ChargeMember> getTermMemberCharges(
            Long termId, Long termMemberId, ChargeMemberPaymentStatus paymentStatus,
            Pageable pageable
    ) {
        return chargeMemberRepository.findTermMemberCharges(
                termId, termMemberId, paymentStatus, pageable);
    }

    public List<LedgerChargeDto> getLedgerCharges(
            Long termId,
            LocalDate fromDate,
            LocalDate toDate,
            boolean includeDeleted,
            Long fundingId
    ) {
        return chargeRepository.findLedgerCharges(
                termId, fromDate, toDate, includeDeleted, fundingId);
    }

    public Long calculatePaidAmount(Long termId) {
        return getLedgerCharges(termId, null, null, false, null).stream()
                .mapToLong(LedgerChargeDto::paidAmount)
                .sum();
    }
}
