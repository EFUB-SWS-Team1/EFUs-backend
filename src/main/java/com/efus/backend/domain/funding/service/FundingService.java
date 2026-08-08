package com.efus.backend.domain.funding.service;

import com.efus.backend.domain.funding.dto.request.FundingCreateRequest;
import com.efus.backend.domain.funding.dto.request.FundingUpdateRequest;
import com.efus.backend.domain.funding.dto.response.FundingDetailResponse;
import com.efus.backend.domain.funding.dto.response.FundingListResponse;
import com.efus.backend.domain.funding.dto.response.FundingResponse;
import com.efus.backend.domain.funding.dto.response.FundingSummaryResponse;
import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.funding.repository.FundingRepository;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.transaction.entity.TransactionType;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
import com.efus.backend.domain.ledger.dto.response.LedgerEntryListResponse;
import com.efus.backend.domain.ledger.entity.LedgerFlowType;
import com.efus.backend.domain.ledger.service.LedgerService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.efus.backend.domain.transaction.repository.TransactionRepository;

import java.time.LocalDate;
import java.time.ZoneId;
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
    private final CurrentUserService currentUserService;
    private final LedgerService ledgerService;


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

    public FundingResponse updateFunding(Long fundingId, FundingUpdateRequest request) {
        Funding funding = fundingQueryService.getFunding(fundingId);
        Long termId = funding.getOrganizationTerm().getId();

        memberQueryService.validateTermMember(termId);
        memberQueryService.validateStaff(termId);
        termQueryService.validateActiveTerm(termId);

        validateUpdateRequest(funding, request);
        funding.update(
                request.name(),
                request.budgetAmount(),
                request.participantCount(),
                request.startDate(),
                request.endDate()
        );

        return FundingResponse.from(funding);
    }

    @Transactional(readOnly = true)
    public LedgerEntryListResponse getLedgerEntries(
            Long fundingId,
            LocalDate fromDate,
            LocalDate toDate,
            LedgerFlowType type,
            boolean includeDeleted,
            int page,
            int size
    ) {
        Funding funding = fundingQueryService.getFunding(fundingId);
        Long termId = funding.getOrganizationTerm().getId();

        memberQueryService.validateTermMember(termId);

        return ledgerService.getLedgerEntries(
                termId, fromDate, toDate, type, includeDeleted, fundingId, page, size);
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
        if (page < 0 || size <= 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_PARAMETER);
        }
        Pageable pageable = PageRequest.of(page, size);

        // 권한 검증
        termQueryService.getTerm(termId);
        memberQueryService.validateTermMember(termId);

        // 행사 목록 페이징 조회
        Page<Funding> fundingPage = fundingRepository.findByOrganizationTerm_Id(termId, pageable);

        // 서버의 현재 날짜 세팅
        LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 데이터 변환 및 지출액 합산
        List<FundingListResponse.FundingDetailDto> fundingDtos = fundingPage.stream()
                .map(funding -> {
                    Long spentAmount = transactionRepository.sumAmountByFundingIdAndTransactionType(
                            funding.getId(),
                            TransactionType.EXPENSE
                    );
                    return FundingListResponse.FundingDetailDto.of(funding, spentAmount, currentDate);
                })
                .toList();

        return new FundingListResponse(
                fundingDtos,
                (long) fundingPage.getNumber(),
                (long) fundingPage.getSize(),
                fundingPage.getTotalElements(),
                (long) fundingPage.getTotalPages(),
                fundingPage.isFirst(),
                fundingPage.isLast()
        );
    }

    // [행사 예산 요약 조회]
    @Transactional(readOnly = true)
    public FundingSummaryResponse getFundingSummary(Long termId) {
        termQueryService.getTerm(termId);
        memberQueryService.validateTermMember(termId);

        // 총 예산, 행사 개수, 총 지출액
        Long totalBudgetAmount = fundingRepository.sumBudgetAmountByTermId(termId);
        Long fundingCount = fundingRepository.countByOrganizationTerm_Id(termId);
        Long totalSpentAmount = transactionRepository.sumFundingAmountByTermIdAndTransactionType(
                termId,
                TransactionType.EXPENSE
        );

        return FundingSummaryResponse.of(
                termId,
                totalBudgetAmount,
                totalSpentAmount,
                fundingCount
        );
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

    private void validateUpdateRequest(Funding funding, FundingUpdateRequest request) {
        if (request.name() != null && request.name().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (request.budgetAmount() != null && request.budgetAmount() <= 0) {
            throw new CustomException(ErrorCode.INVALID_FUNDING_BUDGET);
        }
        if (request.participantCount() != null && request.participantCount() <= 0) {
            throw new CustomException(ErrorCode.INVALID_PARTICIPANT_COUNT);
        }

        LocalDate finalStartDate = request.startDate() == null
                ? funding.getStartDate() : request.startDate();
        LocalDate finalEndDate = request.endDate() == null
                ? funding.getEndDate() : request.endDate();
        if (finalEndDate.isBefore(finalStartDate)) {
            throw new CustomException(ErrorCode.INVALID_FUNDING_PERIOD);
        }
    }
}
