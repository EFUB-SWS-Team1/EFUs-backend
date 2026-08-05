package com.efus.backend.domain.charge.service;

import com.efus.backend.domain.charge.dto.internal.MemberChargeSummary;
import com.efus.backend.domain.charge.repository.ChargeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargeSummaryService {

    private final ChargeMemberRepository chargeMemberRepository;

    public MemberChargeSummary calculateMemberChargeSummary(Long termId, Long termMemberId) {
        return chargeMemberRepository.summarizeAmountsByTermMember(termId, termMemberId);
    }
}
