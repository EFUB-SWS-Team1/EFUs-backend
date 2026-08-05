package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.efus.backend.domain.charge.dto.request.PaymentReversalRequest;
import com.efus.backend.domain.charge.dto.response.PaymentReversalResponse;
import com.efus.backend.domain.charge.entity.*;
import com.efus.backend.domain.charge.repository.*;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ChargePaymentReversalServiceTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private TermQueryService termQueryService;
    private MemberQueryService memberQueryService;
    private ChargeRepository chargeRepository;
    private ChargeMemberRepository chargeMemberRepository;
    private ChargeHistoryRepository chargeHistoryRepository;
    private ChargeService service;
    private Charge charge;
    private ChargeMember chargeMember;
    private TermMember staff;

    @BeforeEach
    void setUp() {
        termQueryService = mock(TermQueryService.class);
        memberQueryService = mock(MemberQueryService.class);
        chargeRepository = mock(ChargeRepository.class);
        chargeMemberRepository = mock(ChargeMemberRepository.class);
        chargeHistoryRepository = mock(ChargeHistoryRepository.class);
        service = new ChargeService(termQueryService, memberQueryService, null, chargeRepository,
                chargeMemberRepository, chargeHistoryRepository,
                JsonMapper.builder().findAndAddModules().build());

        OrganizationTerm term = mock(OrganizationTerm.class);
        when(term.getId()).thenReturn(10L);
        charge = mock(Charge.class);
        when(charge.getId()).thenReturn(1L);
        when(charge.getTerm()).thenReturn(term);
        staff = mock(TermMember.class);
        TermMember target = mock(TermMember.class);
        when(target.getId()).thenReturn(30L);
        chargeMember = ChargeMember.create(charge, target, 10_000L);
        ReflectionTestUtils.setField(chargeMember, "id", 5L);
        chargeMember.markAsPaid(LocalDateTime.of(2026, 8, 6, 15, 30));

        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(chargeMemberRepository.findById(5L)).thenReturn(Optional.of(chargeMember));
        when(memberQueryService.getCurrentActiveStaff(10L)).thenReturn(staff);
    }

    @Test
    void reversesPaymentAndAppendsHistoryWithReasonAndSnapshots() {
        PaymentReversalResponse response = service.reversePayment(
                1L, 5L, new PaymentReversalRequest("중복 납부 처리"));

        assertThat(response.chargeMemberId()).isEqualTo(5L);
        assertThat(response.termMemberId()).isEqualTo(30L);
        assertThat(response.paymentStatus()).isEqualTo(ChargeMemberPaymentStatus.UNPAID);
        assertThat(response.paidAt()).isNull();
        ArgumentCaptor<ChargeHistory> captor = ArgumentCaptor.forClass(ChargeHistory.class);
        verify(chargeHistoryRepository).save(captor.capture());
        ChargeHistory history = captor.getValue();
        assertThat(history.getCharge()).isSameAs(charge);
        assertThat(history.getChargeMember()).isSameAs(chargeMember);
        assertThat(history.getActorTermMember()).isSameAs(staff);
        assertThat(history.getActionType()).isEqualTo(ChargeHistoryActionType.PAYMENT_REVERSED);
        assertThat(history.getReason()).isEqualTo("중복 납부 처리");
        assertThat(history.getBeforeData()).contains("\"paymentStatus\":\"PAID\"");
        assertThat(history.getBeforeData()).doesNotContain("\"paidAt\":null");
        assertThat(history.getAfterData()).contains("\"paymentStatus\":\"UNPAID\"", "\"paidAt\":null");
        verify(chargeHistoryRepository, never()).delete(any());
    }

    @Test
    void missingChargeFails() {
        when(chargeRepository.findById(99L)).thenReturn(Optional.empty());
        assertError(() -> service.reversePayment(99L, 5L, request()), ErrorCode.CHARGE_NOT_FOUND);
    }

    @Test
    void missingChargeMemberFails() {
        when(chargeMemberRepository.findById(99L)).thenReturn(Optional.empty());
        assertError(() -> service.reversePayment(1L, 99L, request()), ErrorCode.CHARGE_MEMBER_NOT_FOUND);
    }

    @Test
    void memberOfDifferentChargeFails() {
        Charge other = mock(Charge.class);
        when(other.getId()).thenReturn(2L);
        ReflectionTestUtils.setField(chargeMember, "charge", other);
        assertError(() -> service.reversePayment(1L, 5L, request()), ErrorCode.CHARGE_MEMBER_MISMATCH);
    }

    @Test
    void deletedChargeFails() {
        when(charge.isDeleted()).thenReturn(true);
        assertError(() -> service.reversePayment(1L, 5L, request()), ErrorCode.CHARGE_ALREADY_DELETED);
    }

    @Test
    void memberAndInactiveStaffCannotReversePayment() {
        when(memberQueryService.getCurrentActiveStaff(10L))
                .thenThrow(new CustomException(ErrorCode.STAFF_REQUIRED));
        assertError(() -> service.reversePayment(1L, 5L, request()), ErrorCode.STAFF_REQUIRED);

        reset(memberQueryService);
        when(memberQueryService.getCurrentActiveStaff(10L))
                .thenThrow(new CustomException(ErrorCode.INACTIVE_TERM_MEMBER));
        assertError(() -> service.reversePayment(1L, 5L, request()), ErrorCode.INACTIVE_TERM_MEMBER);
    }

    @Test
    void closedTermFails() {
        doThrow(new CustomException(ErrorCode.TERM_CLOSED))
                .when(termQueryService).validateActiveTerm(10L);
        assertError(() -> service.reversePayment(1L, 5L, request()), ErrorCode.TERM_CLOSED);
    }

    @Test
    void unpaidMemberFailsWithoutCreatingHistory() {
        chargeMember.reversePayment();
        assertError(() -> service.reversePayment(1L, 5L, request()), ErrorCode.CHARGE_MEMBER_NOT_PAID);
        verifyNoInteractions(chargeHistoryRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void blankReasonIsRejectedByRequestValidation(String reason) {
        assertThat(validator.validate(new PaymentReversalRequest(reason))).isNotEmpty();
    }

    private PaymentReversalRequest request() {
        return new PaymentReversalRequest("잘못된 납부 처리");
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run).isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(errorCode);
        verifyNoInteractions(chargeHistoryRepository);
    }
}
