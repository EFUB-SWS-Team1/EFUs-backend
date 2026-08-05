package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ChargeDeleteServiceTest {
    private TermQueryService termQueryService;
    private MemberQueryService memberQueryService;
    private ChargeRepository chargeRepository;
    private ChargeMemberRepository chargeMemberRepository;
    private ChargeHistoryRepository chargeHistoryRepository;
    private ChargeService service;
    private OrganizationTerm term;
    private TermMember staff;

    @BeforeEach
    void setUp() {
        termQueryService = mock(TermQueryService.class);
        memberQueryService = mock(MemberQueryService.class);
        chargeRepository = mock(ChargeRepository.class);
        chargeMemberRepository = mock(ChargeMemberRepository.class);
        chargeHistoryRepository = mock(ChargeHistoryRepository.class);
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        service = new ChargeService(termQueryService, memberQueryService, null, chargeRepository,
                chargeMemberRepository, chargeHistoryRepository, objectMapper);
        term = mock(OrganizationTerm.class);
        staff = mock(TermMember.class);
        when(term.getId()).thenReturn(10L);
        when(staff.getId()).thenReturn(20L);
        when(memberQueryService.getCurrentActiveStaff(10L)).thenReturn(staff);
    }

    @Test
    void deletesUnpaidChargeAndStoresDeleteHistoryWithBeforeAndAfterState() {
        Charge charge = charge();
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(chargeMemberRepository.findAllByChargeId(1L)).thenReturn(List.of());

        service.deleteCharge(1L);

        assertThat(charge.isDeleted()).isTrue();
        assertThat(charge.getDeletedAt()).isNotNull();
        assertThat(charge.getDeletedByTermMember()).isSameAs(staff);
        ArgumentCaptor<ChargeHistory> captor = ArgumentCaptor.forClass(ChargeHistory.class);
        verify(chargeHistoryRepository).save(captor.capture());
        ChargeHistory history = captor.getValue();
        assertThat(history.getActionType()).isEqualTo(ChargeHistoryActionType.DELETE);
        assertThat(history.getActorTermMember()).isSameAs(staff);
        assertThat(history.getBeforeData()).contains("\"deleted\":false");
        assertThat(history.getAfterData()).contains("\"deleted\":true", "\"deletedByTermMemberId\":20");
        verify(chargeRepository, never()).delete(any());
        verify(chargeMemberRepository, never()).delete(any());
    }

    @Test
    void missingChargeFailsBeforeAuthorizationAndCreatesNoHistory() {
        when(chargeRepository.findById(99L)).thenReturn(Optional.empty());
        assertError(() -> service.deleteCharge(99L), ErrorCode.CHARGE_NOT_FOUND);
        verifyNoInteractions(memberQueryService, chargeHistoryRepository);
    }

    @Test
    void alreadyDeletedChargeCannotBeDeletedAgain() {
        Charge charge = charge();
        charge.softDelete(staff, java.time.LocalDateTime.now());
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        assertError(() -> service.deleteCharge(1L), ErrorCode.CHARGE_ALREADY_DELETED);
        verifyNoInteractions(chargeHistoryRepository);
    }

    @Test
    void memberAuthorizationFailureLeavesChargeUnchangedAndCreatesNoHistory() {
        Charge charge = charge();
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(memberQueryService.getCurrentActiveStaff(10L))
                .thenThrow(new CustomException(ErrorCode.STAFF_REQUIRED));
        assertError(() -> service.deleteCharge(1L), ErrorCode.STAFF_REQUIRED);
        assertThat(charge.isDeleted()).isFalse();
        verifyNoInteractions(chargeHistoryRepository);
    }

    @Test
    void closedTermLeavesChargeUnchangedAndCreatesNoHistory() {
        Charge charge = charge();
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        doThrow(new CustomException(ErrorCode.TERM_CLOSED)).when(termQueryService).validateActiveTerm(10L);
        assertError(() -> service.deleteCharge(1L), ErrorCode.TERM_CLOSED);
        assertThat(charge.isDeleted()).isFalse();
        verifyNoInteractions(chargeHistoryRepository);
    }

    @Test
    void currentPaidMemberBlocksDeletionRegardlessOfPastHistory() {
        Charge charge = charge();
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(chargeMemberRepository.existsByChargeIdAndPaymentStatus(
                1L, ChargeMemberPaymentStatus.PAID)).thenReturn(true);
        assertError(() -> service.deleteCharge(1L), ErrorCode.CHARGE_HAS_PAYMENT);
        assertThat(charge.isDeleted()).isFalse();
        verifyNoInteractions(chargeHistoryRepository);
    }

    private Charge charge() {
        return Charge.create(term, null, staff, "회비", ChargeMethod.PER_PERSON,
                LocalDate.of(2026, 9, 1), null, 10_000L);
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run).isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(errorCode);
    }
}
