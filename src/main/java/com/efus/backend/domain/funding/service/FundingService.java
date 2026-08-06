package com.efus.backend.domain.funding.service;

import com.efus.backend.domain.funding.dto.request.FundingCreateRequest;
import com.efus.backend.domain.funding.dto.response.FundingDetailResponse;
import com.efus.backend.domain.funding.dto.response.FundingListResponse;
import com.efus.backend.domain.funding.dto.response.FundingResponse;
import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.funding.repository.FundingRepository;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
 import com.efus.backend.domain.member.entity.TermMember;
 import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.transaction.entity.TransactionType;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 import com.efus.backend.domain.transaction.repository.TransactionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FundingService {

    private final FundingRepository fundingRepository;
    private final TermQueryService termQueryService;
    private final FundingQueryService fundingQueryService;
    private final MemberQueryService memberQueryService;
    private final TransactionRepository transactionRepository;

    public FundingResponse createFunding(Long termId, FundingCreateRequest request) {
        OrganizationTerm term = termQueryService.getTerm(termId);

        TermMember currentTermMember = memberQueryService.getCurrentTermMember(termId);

        memberQueryService.validateStaff(termId);

        termQueryService.validateActiveTerm(termId);

        validateCreateRequest(request);

        Funding funding = Funding.builder()
                .organizationTerm(term)
                .createdByTermMember(currentTermMember)
                .name(request.name())
                .budgetAmount(request.budgetAmount())
                .participantCount(request.participantCount())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        Funding savedFunding = fundingRepository.save(funding);

        return FundingResponse.from(savedFunding);
    }

    @Transactional(readOnly = true)
    public FundingDetailResponse getFundingDetail(Long termId, Long fundingId) {
        Funding funding = fundingQueryService.getFundingInTerm(termId, fundingId);

         memberQueryService.validateTermMember(termId);

         Long spentAmount = transactionRepository.sumAmountByFundingIdAndTransactionType(
                 fundingId,
                 TransactionType.EXPENSE
         );

         List<FundingDetailResponse.FundingTransactionResponse> transactions =
                 transactionRepository.findAllByFunding_IdAndDeletedFalseOrderByTransactionDateDesc(fundingId)
                         .stream()
                         .map(FundingDetailResponse.FundingTransactionResponse::from)
                         .toList();

        return FundingDetailResponse.of(funding, spentAmount, transactions);
    }

    // [행사 목록 조회]
    @Transactional(readOnly = true)
    public FundingListResponse getFundingList(Long termId, int page, int size) {

    }

    private void validateCreateRequest(FundingCreateRequest request) {
        if (request.budgetAmount() <= 0) {
            throw new CustomException(ErrorCode.INVALID_FUNDING_BUDGET);
        }

        if (request.participantCount() <= 0) {
            throw new CustomException(ErrorCode.INVALID_PARTICIPANT_COUNT);
        }

        if (request.endDate().isBefore(request.startDate())) {
            throw new CustomException(ErrorCode.INVALID_FUNDING_PERIOD);
        }
    }
}