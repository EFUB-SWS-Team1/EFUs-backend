package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.dto.response.ChargeDetailResponse;
import com.efus.backend.domain.charge.entity.Charge;
import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import com.efus.backend.domain.charge.entity.ChargePaymentStatus;
import com.efus.backend.domain.charge.repository.ChargeMemberRepository;
import com.efus.backend.domain.charge.repository.ChargeRepository;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChargeDetailServiceTest {

    private MemberQueryService memberQueryService;
    private ChargeRepository chargeRepository;
    private ChargeMemberRepository chargeMemberRepository;
    private ChargeService chargeService;

    @BeforeEach
    void setUp() {
        memberQueryService = mock(MemberQueryService.class);
        chargeRepository = mock(ChargeRepository.class);
        chargeMemberRepository = mock(ChargeMemberRepository.class);
        chargeService = new ChargeService(mock(TermQueryService.class), memberQueryService,
                null, chargeRepository, chargeMemberRepository);
    }

    @Test
    void allUnpaidReturnsUnpaidAmountsCountsAndStatus() {
        givenCharge(1L, 10L, 30_000L);
        List<ChargeMember> members = List.of(
                chargeMember(10_000L, ChargeMemberPaymentStatus.UNPAID),
                chargeMember(10_000L, ChargeMemberPaymentStatus.UNPAID),
                chargeMember(10_000L, ChargeMemberPaymentStatus.UNPAID));
        when(chargeMemberRepository.findAllByChargeId(1L)).thenReturn(members);

        ChargeDetailResponse response = chargeService.getChargeDetail(1L);

        assertThat(response.requestedAmount()).isEqualTo(30_000L);
        assertThat(response.paidAmount()).isZero();
        assertThat(response.unpaidAmount()).isEqualTo(30_000L);
        assertThat(response.targetCount()).isEqualTo(3);
        assertThat(response.paidCount()).isZero();
        assertThat(response.unpaidCount()).isEqualTo(3);
        assertThat(response.paymentStatus()).isEqualTo(ChargePaymentStatus.UNPAID);
        verify(memberQueryService).validateTermMember(10L);
    }

    @Test
    void partiallyPaidSumsOnlyPaidMembers() {
        givenCharge(1L, 10L, 30_000L);
        List<ChargeMember> members = List.of(
                chargeMember(10_000L, ChargeMemberPaymentStatus.PAID),
                chargeMember(10_000L, ChargeMemberPaymentStatus.UNPAID),
                chargeMember(10_000L, ChargeMemberPaymentStatus.UNPAID));
        when(chargeMemberRepository.findAllByChargeId(1L)).thenReturn(members);

        ChargeDetailResponse response = chargeService.getChargeDetail(1L);

        assertThat(response.paidAmount()).isEqualTo(10_000L);
        assertThat(response.unpaidAmount()).isEqualTo(20_000L);
        assertThat(response.paidCount()).isEqualTo(1);
        assertThat(response.unpaidCount()).isEqualTo(2);
        assertThat(response.paymentStatus()).isEqualTo(ChargePaymentStatus.PARTIALLY_PAID);
    }

    @Test
    void equalSplitUsesPersistedRequestedAmountAndReturnsPaid() {
        givenCharge(1L, 10L, 9_999L);
        List<ChargeMember> members = List.of(
                chargeMember(3_333L, ChargeMemberPaymentStatus.PAID),
                chargeMember(3_333L, ChargeMemberPaymentStatus.PAID),
                chargeMember(3_333L, ChargeMemberPaymentStatus.PAID));
        when(chargeMemberRepository.findAllByChargeId(1L)).thenReturn(members);

        ChargeDetailResponse response = chargeService.getChargeDetail(1L);

        assertThat(response.requestedAmount()).isEqualTo(9_999L);
        assertThat(response.paidAmount()).isEqualTo(9_999L);
        assertThat(response.unpaidAmount()).isZero();
        assertThat(response.paymentStatus()).isEqualTo(ChargePaymentStatus.PAID);
    }

    @Test
    void missingChargeThrowsChargeNotFoundBeforeMembershipValidation() {
        when(chargeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeService.getChargeDetail(99L))
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CHARGE_NOT_FOUND);
    }

    private void givenCharge(Long chargeId, Long termId, Long requestedAmount) {
        OrganizationTerm term = mock(OrganizationTerm.class);
        when(term.getId()).thenReturn(termId);
        Charge charge = mock(Charge.class);
        when(charge.getId()).thenReturn(chargeId);
        when(charge.getTerm()).thenReturn(term);
        when(charge.getRequestedAmount()).thenReturn(requestedAmount);
        when(chargeRepository.findById(chargeId)).thenReturn(Optional.of(charge));
    }

    private ChargeMember chargeMember(Long assignedAmount, ChargeMemberPaymentStatus status) {
        ChargeMember member = mock(ChargeMember.class);
        when(member.getAssignedAmount()).thenReturn(assignedAmount);
        when(member.getPaymentStatus()).thenReturn(status);
        return member;
    }
}
