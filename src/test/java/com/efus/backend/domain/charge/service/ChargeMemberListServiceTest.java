package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.dto.request.ChargeMemberListRequest;
import com.efus.backend.domain.charge.dto.response.ChargeMemberListResponse;
import com.efus.backend.domain.charge.entity.Charge;
import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import com.efus.backend.domain.charge.repository.ChargeMemberRepository;
import com.efus.backend.domain.charge.repository.ChargeRepository;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ChargeMemberListServiceTest {

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
    void returnsMemberFieldsAndPagination() {
        givenCharge(1L, 10L);
        LocalDateTime paidAt = LocalDateTime.of(2026, 8, 5, 18, 30);
        List<ChargeMember> content = List.of(
                chargeMember(3L, "Kim", TermMemberRole.MEMBER, 10_000L,
                        ChargeMemberPaymentStatus.PAID, paidAt),
                chargeMember(5L, "Lee", TermMemberRole.STAFF, 10_000L,
                        ChargeMemberPaymentStatus.UNPAID, null));
        when(chargeMemberRepository.findChargeMembers(1L, null, null, PageRequest.of(0, 7)))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 7), 9));

        ChargeMemberListResponse response = chargeService.getChargeMembers(1L, request(null, null, 0, 7));

        assertThat(response.members()).hasSize(2);
        assertThat(response.members().get(0).termMemberId()).isEqualTo(3L);
        assertThat(response.members().get(0).name()).isEqualTo("Kim");
        assertThat(response.members().get(0).role()).isEqualTo(TermMemberRole.MEMBER);
        assertThat(response.members().get(0).assignedAmount()).isEqualTo(10_000L);
        assertThat(response.members().get(0).paymentStatus()).isEqualTo(ChargeMemberPaymentStatus.PAID);
        assertThat(response.members().get(0).paidAt()).isEqualTo(paidAt);
        assertThat(response.members().get(1).paidAt()).isNull();
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(7);
        assertThat(response.totalElements()).isEqualTo(9);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();
        verify(memberQueryService).validateTermMember(10L);
    }

    @Test
    void trimsKeywordAndAppliesPaymentStatusAndPage() {
        givenCharge(1L, 10L);
        PageRequest pageable = PageRequest.of(2, 3);
        when(chargeMemberRepository.findChargeMembers(
                1L, "Kim", ChargeMemberPaymentStatus.UNPAID, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        ChargeMemberListResponse response = chargeService.getChargeMembers(
                1L, request("  Kim  ", ChargeMemberPaymentStatus.UNPAID, 2, 3));

        assertThat(response.members()).isEmpty();
        verify(chargeMemberRepository).findChargeMembers(
                1L, "Kim", ChargeMemberPaymentStatus.UNPAID, pageable);
    }

    @Test
    void blankKeywordIsIgnored() {
        givenCharge(1L, 10L);
        PageRequest pageable = PageRequest.of(0, 7);
        when(chargeMemberRepository.findChargeMembers(1L, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        chargeService.getChargeMembers(1L, request("   ", null, 0, 7));

        verify(chargeMemberRepository).findChargeMembers(1L, null, null, pageable);
    }

    @Test
    void missingChargeThrowsChargeNotFoundBeforeMembershipValidation() {
        when(chargeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chargeService.getChargeMembers(99L, request(null, null, 0, 7)))
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(ErrorCode.CHARGE_NOT_FOUND);
    }

    private void givenCharge(Long chargeId, Long termId) {
        OrganizationTerm term = mock(OrganizationTerm.class);
        when(term.getId()).thenReturn(termId);
        Charge charge = mock(Charge.class);
        when(charge.getTerm()).thenReturn(term);
        when(chargeRepository.findById(chargeId)).thenReturn(Optional.of(charge));
    }

    private ChargeMember chargeMember(Long termMemberId, String name, TermMemberRole role,
                                      Long amount, ChargeMemberPaymentStatus status, LocalDateTime paidAt) {
        User user = mock(User.class);
        when(user.getName()).thenReturn(name);
        TermMember termMember = mock(TermMember.class);
        when(termMember.getId()).thenReturn(termMemberId);
        when(termMember.getUser()).thenReturn(user);
        when(termMember.getRole()).thenReturn(role);
        ChargeMember chargeMember = mock(ChargeMember.class);
        when(chargeMember.getTermMember()).thenReturn(termMember);
        when(chargeMember.getAssignedAmount()).thenReturn(amount);
        when(chargeMember.getPaymentStatus()).thenReturn(status);
        when(chargeMember.getPaidAt()).thenReturn(paidAt);
        return chargeMember;
    }

    private ChargeMemberListRequest request(String keyword, ChargeMemberPaymentStatus status, int page, int size) {
        ChargeMemberListRequest request = new ChargeMemberListRequest();
        request.setKeyword(keyword);
        request.setPaymentStatus(status);
        request.setPage(page);
        request.setSize(size);
        return request;
    }
}
