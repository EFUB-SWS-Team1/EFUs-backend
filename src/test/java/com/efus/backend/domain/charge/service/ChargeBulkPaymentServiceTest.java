package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.efus.backend.domain.charge.dto.request.BulkPaymentRequest;
import com.efus.backend.domain.charge.dto.response.BulkPaymentResponse;
import com.efus.backend.domain.charge.entity.*;
import com.efus.backend.domain.charge.repository.*;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ChargeBulkPaymentServiceTest {
    private TermQueryService termQueryService;
    private MemberQueryService memberQueryService;
    private ChargeMemberRepository chargeMemberRepository;
    private ChargeHistoryRepository chargeHistoryRepository;
    private ChargeService service;
    private Charge charge;
    private ChargeMember first;
    private ChargeMember second;

    @BeforeEach
    void setUp() {
        termQueryService = mock(TermQueryService.class);
        memberQueryService = mock(MemberQueryService.class);
        ChargeRepository chargeRepository = mock(ChargeRepository.class);
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
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(memberQueryService.getCurrentActiveStaff(10L)).thenReturn(mock(TermMember.class));
        first = member(101L, charge);
        second = member(102L, charge);
    }

    @Test
    void bulkPaymentAllUnpaid_success() {
        when(chargeMemberRepository.findAllByChargeIdAndPaymentStatus(
                1L, ChargeMemberPaymentStatus.UNPAID)).thenReturn(List.of(first, second));

        BulkPaymentResponse response = service.bulkPayment(1L,
                new BulkPaymentRequest(BulkPaymentTargetMode.ALL_UNPAID, null, null));

        assertThat(response.processedCount()).isEqualTo(2);
        assertThat(first.getPaymentStatus()).isEqualTo(ChargeMemberPaymentStatus.PAID);
        assertThat(second.getPaymentStatus()).isEqualTo(ChargeMemberPaymentStatus.PAID);
        assertThat(first.getPaidAt()).isEqualTo(second.getPaidAt()).isEqualTo(response.paidAt());
        verify(chargeHistoryRepository, times(2)).save(any(ChargeHistory.class));
    }

    @Test
    void bulkPaymentSelected_usesRequestedTimeAndCreatesOneHistoryPerMember() {
        LocalDateTime paidAt = LocalDateTime.of(2026, 8, 6, 14, 30);
        when(chargeMemberRepository.findAllByIdIn(List.of(101L, 102L)))
                .thenReturn(List.of(first, second));

        BulkPaymentResponse response = service.bulkPayment(1L,
                new BulkPaymentRequest(BulkPaymentTargetMode.SELECTED,
                        List.of(101L, 102L), paidAt));

        assertThat(response).isEqualTo(new BulkPaymentResponse(1L, 2, paidAt));
        assertThat(first.getPaidAt()).isEqualTo(paidAt);
        assertThat(second.getPaidAt()).isEqualTo(paidAt);
        ArgumentCaptor<ChargeHistory> captor = ArgumentCaptor.forClass(ChargeHistory.class);
        verify(chargeHistoryRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(history -> {
            assertThat(history.getActionType()).isEqualTo(ChargeHistoryActionType.PAYMENT_COMPLETED);
            assertThat(history.getBeforeData()).contains("\"paymentStatus\":\"UNPAID\"");
            assertThat(history.getAfterData()).contains("\"paymentStatus\":\"PAID\"");
        });
    }

    @Test
    void selectedInvalidIds_failBeforeQuery() {
        assertError(new BulkPaymentRequest(BulkPaymentTargetMode.SELECTED, List.of(), null),
                ErrorCode.INVALID_CHARGE_REQUEST);
        assertError(new BulkPaymentRequest(BulkPaymentTargetMode.SELECTED, List.of(1L, 1L), null),
                ErrorCode.INVALID_CHARGE_REQUEST);
        verifyNoInteractions(chargeMemberRepository, chargeHistoryRepository);
    }

    @Test
    void selectedMissingMember_failsWithoutMutation() {
        when(chargeMemberRepository.findAllByIdIn(List.of(101L, 999L))).thenReturn(List.of(first));

        assertError(new BulkPaymentRequest(BulkPaymentTargetMode.SELECTED,
                List.of(101L, 999L), null), ErrorCode.CHARGE_MEMBER_NOT_FOUND);

        assertThat(first.getPaymentStatus()).isEqualTo(ChargeMemberPaymentStatus.UNPAID);
        verifyNoInteractions(chargeHistoryRepository);
    }

    @Test
    void selectedMemberFromDifferentCharge_failsWithoutMutation() {
        Charge otherCharge = mock(Charge.class);
        when(otherCharge.getId()).thenReturn(2L);
        ChargeMember other = member(102L, otherCharge);
        when(chargeMemberRepository.findAllByIdIn(List.of(101L, 102L)))
                .thenReturn(List.of(first, other));

        assertError(new BulkPaymentRequest(BulkPaymentTargetMode.SELECTED,
                List.of(101L, 102L), null), ErrorCode.CHARGE_MEMBER_MISMATCH);

        assertThat(first.getPaymentStatus()).isEqualTo(ChargeMemberPaymentStatus.UNPAID);
        verifyNoInteractions(chargeHistoryRepository);
    }

    @Test
    void selectedAlreadyPaid_failsWithoutOverwritingPaidAtOrCreatingHistory() {
        LocalDateTime original = LocalDateTime.of(2026, 8, 1, 10, 0);
        second.markAsPaid(original);
        when(chargeMemberRepository.findAllByIdIn(List.of(101L, 102L)))
                .thenReturn(List.of(first, second));

        assertError(new BulkPaymentRequest(BulkPaymentTargetMode.SELECTED,
                List.of(101L, 102L), original.plusDays(1)), ErrorCode.CHARGE_MEMBER_ALREADY_PAID);

        assertThat(first.getPaymentStatus()).isEqualTo(ChargeMemberPaymentStatus.UNPAID);
        assertThat(second.getPaidAt()).isEqualTo(original);
        verifyNoInteractions(chargeHistoryRepository);
    }

    private ChargeMember member(Long id, Charge owner) {
        TermMember target = mock(TermMember.class);
        ChargeMember result = ChargeMember.create(owner, target, 10_000L);
        ReflectionTestUtils.setField(result, "id", id);
        return result;
    }

    private void assertError(BulkPaymentRequest request, ErrorCode errorCode) {
        assertThatThrownBy(() -> service.bulkPayment(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(errorCode);
    }
}
