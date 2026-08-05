package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.efus.backend.domain.charge.dto.request.ChargePaymentRequest;
import com.efus.backend.domain.charge.dto.response.ChargePaymentResponse;
import com.efus.backend.domain.charge.entity.*;
import com.efus.backend.domain.charge.repository.*;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ChargePaymentServiceTest {
    private TermQueryService termQueryService;
    private MemberQueryService memberQueryService;
    private ChargeRepository chargeRepository;
    private ChargeMemberRepository chargeMemberRepository;
    private ChargeHistoryRepository chargeHistoryRepository;
    private ChargeService service;
    private Charge charge;
    private ChargeMember member;
    private TermMember staff;

    @BeforeEach
    void setUp() {
        termQueryService = mock(TermQueryService.class);
        memberQueryService = mock(MemberQueryService.class);
        chargeRepository = mock(ChargeRepository.class);
        chargeMemberRepository = mock(ChargeMemberRepository.class);
        chargeHistoryRepository = mock(ChargeHistoryRepository.class);
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        service = new ChargeService(termQueryService, memberQueryService, null, chargeRepository,
                chargeMemberRepository, chargeHistoryRepository, mapper);

        OrganizationTerm term = mock(OrganizationTerm.class);
        when(term.getId()).thenReturn(10L);
        charge = mock(Charge.class);
        when(charge.getId()).thenReturn(1L);
        when(charge.getTerm()).thenReturn(term);
        staff = mock(TermMember.class);
        TermMember target = mock(TermMember.class);
        when(target.getId()).thenReturn(30L);
        member = ChargeMember.create(charge, target, 10_000L);
        ReflectionTestUtils.setField(member, "id", 5L);

        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(chargeMemberRepository.findById(5L)).thenReturn(Optional.of(member));
        when(memberQueryService.getCurrentActiveStaff(10L)).thenReturn(staff);
    }

    @Test
    void paysAtRequestedTimeAndCreatesHistory() {
        LocalDateTime paidAt = LocalDateTime.of(2026, 8, 6, 15, 30);
        ChargePaymentResponse response = service.pay(1L, 5L, new ChargePaymentRequest(paidAt));

        assertThat(response.paymentStatus()).isEqualTo(ChargeMemberPaymentStatus.PAID);
        assertThat(response.paidAt()).isEqualTo(paidAt);
        assertThat(response.chargeId()).isEqualTo(1L);
        assertThat(response.chargeMemberId()).isEqualTo(5L);
        ArgumentCaptor<ChargeHistory> captor = ArgumentCaptor.forClass(ChargeHistory.class);
        verify(chargeHistoryRepository).save(captor.capture());
        ChargeHistory history = captor.getValue();
        assertThat(history.getChargeMember()).isSameAs(member);
        assertThat(history.getActorTermMember()).isSameAs(staff);
        assertThat(history.getActionType()).isEqualTo(ChargeHistoryActionType.PAYMENT_COMPLETED);
        assertThat(history.getBeforeData()).contains("\"paymentStatus\":\"UNPAID\"", "\"paidAt\":null");
        assertThat(history.getAfterData()).contains("\"paymentStatus\":\"PAID\"");
        assertThat(history.getChangedAt()).isNotNull();
        verify(termQueryService).validateActiveTerm(10L);
    }

    @Test
    void omittedRequestUsesCurrentTime() {
        LocalDateTime before = LocalDateTime.now();
        ChargePaymentResponse response = service.pay(1L, 5L, null);
        LocalDateTime after = LocalDateTime.now();
        assertThat(response.paidAt()).isBetween(before, after);
    }

    @Test
    void missingChargeFails() {
        when(chargeRepository.findById(99L)).thenReturn(Optional.empty());
        assertError(() -> service.pay(99L, 5L, null), ErrorCode.CHARGE_NOT_FOUND);
        verifyNoInteractions(chargeHistoryRepository);
    }

    @Test
    void missingChargeMemberFails() {
        when(chargeMemberRepository.findById(99L)).thenReturn(Optional.empty());
        assertError(() -> service.pay(1L, 99L, null), ErrorCode.CHARGE_MEMBER_NOT_FOUND);
        verifyNoInteractions(chargeHistoryRepository);
    }

    @Test
    void memberOfDifferentChargeFails() {
        Charge other = mock(Charge.class);
        when(other.getId()).thenReturn(2L);
        ReflectionTestUtils.setField(member, "charge", other);
        assertError(() -> service.pay(1L, 5L, null), ErrorCode.CHARGE_MEMBER_MISMATCH);
        verifyNoInteractions(chargeHistoryRepository);
    }

    @Test
    void alreadyPaidFailsWithoutChangingTimeOrCreatingHistory() {
        LocalDateTime original = LocalDateTime.of(2026, 8, 1, 10, 0);
        member.markAsPaid(original);
        assertError(() -> service.pay(1L, 5L, new ChargePaymentRequest(original.plusDays(1))),
                ErrorCode.CHARGE_MEMBER_ALREADY_PAID);
        assertThat(member.getPaidAt()).isEqualTo(original);
        verifyNoInteractions(chargeHistoryRepository);
    }

    @Test
    void authorizationAndClosedTermFailuresCreateNoHistory() {
        when(memberQueryService.getCurrentActiveStaff(10L))
                .thenThrow(new CustomException(ErrorCode.STAFF_REQUIRED));
        assertError(() -> service.pay(1L, 5L, null), ErrorCode.STAFF_REQUIRED);
        verifyNoInteractions(chargeHistoryRepository);

        reset(memberQueryService);
        when(memberQueryService.getCurrentActiveStaff(10L)).thenReturn(staff);
        doThrow(new CustomException(ErrorCode.TERM_CLOSED))
                .when(termQueryService).validateActiveTerm(10L);
        assertError(() -> service.pay(1L, 5L, null), ErrorCode.TERM_CLOSED);
        verifyNoInteractions(chargeHistoryRepository);
    }

    @Test
    void deletedChargeFailsBeforeAuthorization() {
        when(charge.isDeleted()).thenReturn(true);
        assertError(() -> service.pay(1L, 5L, null), ErrorCode.CHARGE_ALREADY_DELETED);
        verifyNoInteractions(memberQueryService, chargeHistoryRepository);
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run).isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(errorCode);
    }
}
