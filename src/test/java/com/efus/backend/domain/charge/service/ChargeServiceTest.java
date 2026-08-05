package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.dto.request.ChargePreviewRequest;
import com.efus.backend.domain.charge.dto.response.ChargePreviewResponse;
import com.efus.backend.domain.charge.entity.ChargeMethod;
import com.efus.backend.domain.charge.entity.ChargeTargetMode;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChargeServiceTest {

    private TermQueryService termQueryService;
    private MemberQueryService memberQueryService;
    private ChargeService chargeService;

    @BeforeEach
    void setUp() {
        termQueryService = mock(TermQueryService.class);
        memberQueryService = mock(MemberQueryService.class);
        chargeService = new ChargeService(termQueryService, memberQueryService);
    }

    @Test
    void perPersonAllActiveCalculatesActualTotal() {
        List<TermMember> members = List.of(member(3L, "C"), member(1L, "A"), member(2L, "B"));
        when(memberQueryService.getAllActiveMembers(1L)).thenReturn(members);

        ChargePreviewResponse response = chargeService.preview(
                1L,
                request(ChargeMethod.PER_PERSON, ChargeTargetMode.ALL_ACTIVE, null, 10_000L, null)
        );

        assertThat(response.totalAmount()).isEqualTo(30_000L);
        assertThat(response.requestedAmount()).isEqualTo(30_000L);
        assertThat(response.remainderAmount()).isZero();
        assertThat(response.targetCount()).isEqualTo(3);
        assertThat(response.members())
                .extracting(member -> member.termMemberId())
                .containsExactly(1L, 2L, 3L);
        assertThat(response.members())
                .extracting(member -> member.assignedAmount())
                .containsOnly(10_000L);
        verify(memberQueryService).validateActiveStaff(1L);
        verify(termQueryService).validateActiveTerm(1L);
    }

    @Test
    void perPersonSelectedUsesOnlySelectedMembers() {
        List<TermMember> members = List.of(member(3L, "C"), member(1L, "A"));
        when(memberQueryService.getSelectedActiveMembers(1L, List.of(1L, 3L))).thenReturn(members);

        ChargePreviewResponse response = chargeService.preview(
                1L,
                request(ChargeMethod.PER_PERSON, ChargeTargetMode.SELECTED, List.of(1L, 3L), 10_000L, null)
        );

        assertThat(response.targetCount()).isEqualTo(2);
        assertThat(response.requestedAmount()).isEqualTo(20_000L);
        assertThat(response.remainderAmount()).isZero();
    }

    @Test
    void equalSplitKeepsEveryAssignedAmountEqualAndReturnsRemainder() {
        List<TermMember> members = List.of(member(1L, "A"), member(2L, "B"), member(3L, "C"));
        when(memberQueryService.getSelectedActiveMembers(1L, List.of(1L, 2L, 3L)))
                .thenReturn(members);

        ChargePreviewResponse response = chargeService.preview(
                1L,
                request(ChargeMethod.EQUAL_SPLIT, ChargeTargetMode.SELECTED, List.of(1L, 2L, 3L), null, 10_001L)
        );

        assertThat(response.totalAmount()).isEqualTo(10_001L);
        assertThat(response.requestedAmount()).isEqualTo(9_999L);
        assertThat(response.remainderAmount()).isEqualTo(2L);
        assertThat(response.members())
                .extracting(member -> member.assignedAmount())
                .containsExactly(3_333L, 3_333L, 3_333L);
    }

    @Test
    void equalSplitRejectsAmountSmallerThanTargetCount() {
        List<TermMember> members = List.of(member(1L, "A"), member(2L, "B"), member(3L, "C"));
        when(memberQueryService.getAllActiveMembers(1L))
                .thenReturn(members);

        assertError(
                request(ChargeMethod.EQUAL_SPLIT, ChargeTargetMode.ALL_ACTIVE, null, null, 2L),
                ErrorCode.INVALID_CHARGE_REQUEST
        );
    }

    @Test
    void rejectsEmptyDuplicateAndInvalidSelectedTargets() {
        assertError(
                request(ChargeMethod.PER_PERSON, ChargeTargetMode.SELECTED, List.of(), 1L, null),
                ErrorCode.CHARGE_TARGET_REQUIRED
        );
        assertError(
                request(ChargeMethod.PER_PERSON, ChargeTargetMode.SELECTED, List.of(1L, 1L), 1L, null),
                ErrorCode.DUPLICATE_CHARGE_TARGET
        );
        assertError(
                request(ChargeMethod.PER_PERSON, ChargeTargetMode.SELECTED, java.util.Arrays.asList(1L, null), 1L, null),
                ErrorCode.INVALID_CHARGE_TARGET
        );
        assertError(
                request(ChargeMethod.PER_PERSON, ChargeTargetMode.SELECTED, List.of(0L), 1L, null),
                ErrorCode.INVALID_CHARGE_TARGET
        );
    }

    @Test
    void rejectsAmbiguousTargetAndAmountFields() {
        assertError(
                request(ChargeMethod.PER_PERSON, ChargeTargetMode.ALL_ACTIVE, List.of(1L), 1L, null),
                ErrorCode.INVALID_CHARGE_REQUEST
        );
        assertError(
                request(ChargeMethod.PER_PERSON, ChargeTargetMode.ALL_ACTIVE, null, 1L, 1L),
                ErrorCode.INVALID_CHARGE_REQUEST
        );
        assertError(
                request(ChargeMethod.EQUAL_SPLIT, ChargeTargetMode.ALL_ACTIVE, null, 1L, 1L),
                ErrorCode.INVALID_CHARGE_REQUEST
        );
    }

    @Test
    void rejectsNoTargetsAndPerPersonOverflow() {
        when(memberQueryService.getAllActiveMembers(1L)).thenReturn(List.of());
        assertError(
                request(ChargeMethod.PER_PERSON, ChargeTargetMode.ALL_ACTIVE, null, 1L, null),
                ErrorCode.CHARGE_TARGET_NOT_FOUND
        );

        List<TermMember> members = List.of(member(1L, "A"), member(2L, "B"));
        when(memberQueryService.getAllActiveMembers(1L)).thenReturn(members);
        assertError(
                request(ChargeMethod.PER_PERSON, ChargeTargetMode.ALL_ACTIVE, null, Long.MAX_VALUE, null),
                ErrorCode.INVALID_CHARGE_REQUEST
        );
    }

    private void assertError(ChargePreviewRequest request, ErrorCode errorCode) {
        assertThatThrownBy(() -> chargeService.preview(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(errorCode);
    }

    private ChargePreviewRequest request(
            ChargeMethod chargeMethod,
            ChargeTargetMode targetMode,
            List<Long> targetIds,
            Long perPersonAmount,
            Long totalAmount
    ) {
        return new ChargePreviewRequest(chargeMethod, targetMode, targetIds, perPersonAmount, totalAmount);
    }

    private TermMember member(Long id, String name) {
        User user = mock(User.class);
        when(user.getName()).thenReturn(name);

        TermMember member = mock(TermMember.class);
        when(member.getId()).thenReturn(id);
        when(member.getUser()).thenReturn(user);
        return member;
    }
}
