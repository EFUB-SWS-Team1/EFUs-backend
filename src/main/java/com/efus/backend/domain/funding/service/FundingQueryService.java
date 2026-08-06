package com.efus.backend.domain.funding.service;

import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.funding.repository.FundingRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FundingQueryService {

    private final FundingRepository fundingRepository;

    public Funding getFunding(Long fundingId) {
        return fundingRepository.findById(fundingId)
                .orElseThrow(() -> new CustomException(ErrorCode.FUNDING_NOT_FOUND));
    }

    public Funding getFundingInTerm(Long termId, Long fundingId) {
        return fundingRepository.findByIdAndOrganizationTerm_Id(fundingId, termId)
                .orElseThrow(() -> new CustomException(ErrorCode.FUNDING_NOT_FOUND));
    }
}
