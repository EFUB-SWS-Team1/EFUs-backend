package com.efus.backend.domain.funding.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.funding.dto.request.FundingUpdateRequest;
import com.efus.backend.domain.funding.dto.response.FundingResponse;
import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.funding.repository.FundingRepository;
import com.efus.backend.domain.ledger.service.LedgerService;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.transaction.repository.TransactionRepository;
import com.efus.backend.domain.user.service.CurrentUserService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FundingServiceTest {

    private FundingQueryService fundingQueryService;
    private MemberQueryService memberQueryService;
    private TermQueryService termQueryService;
    private FundingService fundingService;
    private Funding funding;

    @BeforeEach
    void setUp() {
        FundingRepository fundingRepository = mock(FundingRepository.class);
        fundingQueryService = mock(FundingQueryService.class);
        memberQueryService = mock(MemberQueryService.class);
        termQueryService = mock(TermQueryService.class);
        OrganizationTerm term = mock(OrganizationTerm.class);
        TermMember creator = mock(TermMember.class);
        when(term.getId()).thenReturn(10L);
        when(creator.getId()).thenReturn(20L);
        funding = Funding.builder()
                .organizationTerm(term)
                .createdByTermMember(creator)
                .name("기존 행사")
                .budgetAmount(100_000L)
                .participantCount(10)
                .startDate(LocalDate.of(2026, 8, 10))
                .endDate(LocalDate.of(2026, 8, 20))
                .build();
        fundingService = new FundingService(
                fundingRepository,
                termQueryService,
                fundingQueryService,
                memberQueryService,
                mock(TransactionRepository.class),
                mock(CurrentUserService.class),
                mock(LedgerService.class)
        );
        when(fundingQueryService.getFunding(1L)).thenReturn(funding);
    }

    @Test
    void updatesOnlyProvidedFieldsAndKeepsOmittedValues() {
        FundingUpdateRequest request = new FundingUpdateRequest(
                "수정 행사", null, 30, null, null);

        FundingResponse response = fundingService.updateFunding(1L, request);

        assertThat(response.name()).isEqualTo("수정 행사");
        assertThat(response.budgetAmount()).isEqualTo(100_000L);
        assertThat(response.participantCount()).isEqualTo(30);
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 8, 20));
        verify(memberQueryService).validateTermMember(10L);
        verify(memberQueryService).validateStaff(10L);
        verify(termQueryService).validateActiveTerm(10L);
    }

    @Test
    void validatesPeriodAgainstFinalFundingState() {
        FundingUpdateRequest request = new FundingUpdateRequest(
                null, null, null, null, LocalDate.of(2026, 8, 5));

        assertThatThrownBy(() -> fundingService.updateFunding(1L, request))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_FUNDING_PERIOD));
    }

    @Test
    void rejectsNonPositiveBudget() {
        FundingUpdateRequest request = new FundingUpdateRequest(
                null, 0L, null, null, null);

        assertThatThrownBy(() -> fundingService.updateFunding(1L, request))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_FUNDING_BUDGET));
    }
}
