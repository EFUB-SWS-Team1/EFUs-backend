package com.efus.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.dto.internal.MemberChargeSummary;
import com.efus.backend.domain.charge.service.ChargeSummaryService;
import com.efus.backend.domain.member.dto.response.TermMemberDetailResponse;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TermMemberDetailServiceTest {

    private TermQueryService termQueryService;
    private TermMemberRepository termMemberRepository;
    private ChargeSummaryService chargeSummaryService;
    private MemberQueryService memberQueryService;

    @BeforeEach
    void setUp() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        termQueryService = mock(TermQueryService.class);
        termMemberRepository = mock(TermMemberRepository.class);
        chargeSummaryService = mock(ChargeSummaryService.class);
        memberQueryService = new MemberQueryService(
                currentUserService, termQueryService, termMemberRepository, chargeSummaryService);

        User currentUser = mock(User.class);
        when(currentUser.getId()).thenReturn(100L);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(termMemberRepository.findByTermIdAndUserId(1L, 100L))
                .thenReturn(Optional.of(mock(TermMember.class)));
    }

    @Test
    void returnsMemberInformationAndMixedPaymentSummary() {
        TermMember target = targetMember();
        when(termMemberRepository.findByIdAndTermId(20L, 1L)).thenReturn(Optional.of(target));
        when(chargeSummaryService.calculateMemberChargeSummary(1L, 20L))
                .thenReturn(new MemberChargeSummary(40_000L, 20_000L));

        TermMemberDetailResponse response = memberQueryService.getTermMemberDetail(1L, 20L);

        assertThat(response.name()).isEqualTo("김이펍");
        assertThat(response.email()).isEqualTo("efub@example.com");
        assertThat(response.role()).isEqualTo(TermMemberRole.MEMBER);
        assertThat(response.paidAmount()).isEqualTo(40_000L);
        assertThat(response.unpaidAmount()).isEqualTo(20_000L);
        verify(termQueryService).getTerm(1L);
    }

    @Test
    void returnsZeroAmountsWhenMemberHasNoCharges() {
        TermMember target = targetMember();
        when(termMemberRepository.findByIdAndTermId(20L, 1L))
                .thenReturn(Optional.of(target));
        when(chargeSummaryService.calculateMemberChargeSummary(1L, 20L))
                .thenReturn(new MemberChargeSummary(0L, 0L));

        TermMemberDetailResponse response = memberQueryService.getTermMemberDetail(1L, 20L);

        assertThat(response.paidAmount()).isZero();
        assertThat(response.unpaidAmount()).isZero();
    }

    @Test
    void missingTermStopsBeforeMembershipAndTargetLookup() {
        when(termQueryService.getTerm(99L))
                .thenThrow(new CustomException(ErrorCode.TERM_NOT_FOUND));

        assertError(() -> memberQueryService.getTermMemberDetail(99L, 20L), ErrorCode.TERM_NOT_FOUND);
        verifyNoInteractions(chargeSummaryService);
    }

    @Test
    void missingOrDifferentTermMemberUsesTermMemberNotFound() {
        when(termMemberRepository.findByIdAndTermId(20L, 1L)).thenReturn(Optional.empty());

        assertError(
                () -> memberQueryService.getTermMemberDetail(1L, 20L),
                ErrorCode.TERM_MEMBER_NOT_FOUND);
        verifyNoInteractions(chargeSummaryService);
    }

    @Test
    void requesterMustBelongToTermWithoutStaffOrActiveValidation() {
        when(termMemberRepository.findByTermIdAndUserId(1L, 100L)).thenReturn(Optional.empty());

        assertError(
                () -> memberQueryService.getTermMemberDetail(1L, 20L),
                ErrorCode.TERM_ACCESS_DENIED);
        verifyNoInteractions(chargeSummaryService);
    }

    private TermMember targetMember() {
        User user = mock(User.class);
        when(user.getName()).thenReturn("김이펍");
        when(user.getEmail()).thenReturn("efub@example.com");
        TermMember target = mock(TermMember.class);
        when(target.getUser()).thenReturn(user);
        when(target.getRole()).thenReturn(TermMemberRole.MEMBER);
        return target;
    }

    private void assertError(Runnable operation, ErrorCode errorCode) {
        assertThatThrownBy(operation::run)
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(errorCode);
    }
}
