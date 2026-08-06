package com.efus.backend.domain.funding.service;

import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.funding.repository.FundingRepository;
import com.efus.backend.domain.transaction.entity.TransactionType;
import com.efus.backend.domain.transaction.repository.TransactionRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FundingQueryService {

    private final FundingRepository fundingRepository;
    private final TransactionRepository transactionRepository;

    public record FundingSpending(Funding funding, Long spentAmount) {
    }

    public Funding getFunding(Long fundingId) {
        return fundingRepository.findById(fundingId)
                .orElseThrow(() -> new CustomException(ErrorCode.FUNDING_NOT_FOUND));
    }

    public Funding getFundingInTerm(Long termId, Long fundingId) {
        return fundingRepository.findByIdAndOrganizationTerm_Id(fundingId, termId)
                .orElseThrow(() -> new CustomException(ErrorCode.FUNDING_NOT_FOUND));
    }

    public List<FundingSpending> getFundingSpendings(Long termId) {
        return fundingRepository.findByOrganizationTerm_Id(termId, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(funding -> new FundingSpending(
                        funding,
                        transactionRepository.sumAmountByFundingIdAndTransactionType(
                                funding.getId(), TransactionType.EXPENSE)))
                .toList();
    }
}
