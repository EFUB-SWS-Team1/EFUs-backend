package com.efus.backend.domain.funding.service;

import com.efus.backend.domain.funding.dto.request.FundingCreateRequest;
import com.efus.backend.domain.funding.dto.response.FundingResponse;
import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.funding.repository.FundingRepository;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
 import com.efus.backend.domain.member.entity.TermMember;
// TODO: MemberQueryService 병합 후 주석 해제
// import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FundingService {

    private final FundingRepository fundingRepository;
    private final TermQueryService termQueryService;
    // TODO: MemberQueryService 병합 후 주석 해제
    // private final MemberQueryService memberQueryService;

    public FundingResponse createFunding(Long termId, FundingCreateRequest request) {
        OrganizationTerm term = termQueryService.getTerm(termId);

        // TODO: MemberQueryService 병합 후 주석 해제
        // TermMember currentTermMember = memberQueryService.getCurrentTermMember(termId);

        // TODO: MemberQueryService 병합 후 주석 해제
        // memberQueryService.validateStaff(termId);

        termQueryService.validateActiveTerm(termId);

        validateCreateRequest(request);

        Funding funding = Funding.builder()
                .organizationTerm(term)
                // TODO: MemberQueryService 병합 후 주석 해제
                // .createdByTermMember(currentTermMember)
                .name(request.name())
                .budgetAmount(request.budgetAmount())
                .participantCount(request.participantCount())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        Funding savedFunding = fundingRepository.save(funding);

        return FundingResponse.from(savedFunding);
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