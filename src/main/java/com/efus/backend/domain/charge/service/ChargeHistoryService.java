package com.efus.backend.domain.charge.service;

import com.efus.backend.domain.charge.dto.request.ChargeHistoryListRequest;
import com.efus.backend.domain.charge.dto.response.ChargeHistoryListResponse;
import com.efus.backend.domain.charge.dto.response.ChargeHistoryResponse;
import com.efus.backend.domain.charge.entity.Charge;
import com.efus.backend.domain.charge.repository.ChargeHistoryRepository;
import com.efus.backend.domain.charge.repository.ChargeRepository;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargeHistoryService {

    private final ChargeRepository chargeRepository;
    private final ChargeHistoryRepository chargeHistoryRepository;
    private final MemberQueryService memberQueryService;

    public ChargeHistoryListResponse getChargeHistories(
            Long chargeId,
            ChargeHistoryListRequest request
    ) {
        Charge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHARGE_NOT_FOUND));
        memberQueryService.validateTermMember(charge.getTerm().getId());

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<ChargeHistoryResponse> histories = chargeHistoryRepository
                .findAllByCharge_IdOrderByChangedAtDesc(chargeId, pageable)
                .map(ChargeHistoryResponse::from);

        return ChargeHistoryListResponse.from(histories);
    }
}
