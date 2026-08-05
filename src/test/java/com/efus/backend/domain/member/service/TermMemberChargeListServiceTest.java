package com.efus.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.entity.Charge;
import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import com.efus.backend.domain.charge.service.ChargeSummaryService;
import com.efus.backend.domain.member.dto.request.TermMemberChargeListRequest;
import com.efus.backend.domain.member.dto.response.TermMemberChargeListResponse;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class TermMemberChargeListServiceTest {

    private TermQueryService termQueryService;
    private TermMemberRepository termMemberRepository;
    private ChargeSummaryService chargeSummaryService;
    private MemberQueryService service;

    @BeforeEach
    void setUp() {
        termQueryService = mock(TermQueryService.class);
        termMemberRepository = mock(TermMemberRepository.class);
        chargeSummaryService = mock(ChargeSummaryService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        User currentUser = mock(User.class);
        when(currentUser.getId()).thenReturn(100L);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        service = new MemberQueryService(
                currentUserService,
                termQueryService, termMemberRepository, chargeSummaryService);

        OrganizationTerm term = mock(OrganizationTerm.class);
        when(term.getId()).thenReturn(1L);
        TermMember target = mock(TermMember.class);
        when(target.getTerm()).thenReturn(term);
        when(termMemberRepository.findById(2L)).thenReturn(Optional.of(target));
        TermMember current = mock(TermMember.class);
        when(termMemberRepository.findByTermIdAndUserId(1L, 100L)).thenReturn(Optional.of(current));
    }

    @Test
    void returnsAllStatusesAndMapsResponse() {
        TermMemberChargeListRequest request = request(null, 0, 7);
        ChargeMember unpaid = chargeMember(10L, "회비", 12_000L,
                LocalDate.of(2026, 8, 31), ChargeMemberPaymentStatus.UNPAID);
        ChargeMember paid = chargeMember(11L, "행사비", 5_000L,
                LocalDate.of(2026, 8, 20), ChargeMemberPaymentStatus.PAID);
        PageRequest pageable = PageRequest.of(0, 7);
        when(chargeSummaryService.getTermMemberCharges(1L, 2L, null, pageable))
                .thenReturn(new PageImpl<>(List.of(unpaid, paid), pageable, 2));

        TermMemberChargeListResponse response = service.getTermMemberCharges(1L, 2L, request);

        assertThat(response.charges()).hasSize(2);
        assertThat(response.charges().get(0))
                .extracting("chargeId", "title", "assignedAmount", "dueDate", "paymentStatus")
                .containsExactly(10L, "회비", 12_000L, LocalDate.of(2026, 8, 31),
                        ChargeMemberPaymentStatus.UNPAID);
    }

    @ParameterizedTest
    @EnumSource(ChargeMemberPaymentStatus.class)
    void passesPaymentStatusFilter(ChargeMemberPaymentStatus status) {
        TermMemberChargeListRequest request = request(status, 2, 3);
        PageRequest pageable = PageRequest.of(2, 3);
        when(chargeSummaryService.getTermMemberCharges(1L, 2L, status, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.getTermMemberCharges(1L, 2L, request);

        verify(chargeSummaryService).getTermMemberCharges(1L, 2L, status, pageable);
    }

    @Test
    void rejectsMissingTermMember() {
        when(termMemberRepository.findById(2L)).thenReturn(Optional.empty());

        assertError(() -> service.getTermMemberCharges(1L, 2L, request(null, 0, 7)),
                ErrorCode.TERM_MEMBER_NOT_FOUND);
    }

    @Test
    void rejectsTermMemberFromAnotherTerm() {
        OrganizationTerm anotherTerm = mock(OrganizationTerm.class);
        when(anotherTerm.getId()).thenReturn(9L);
        TermMember target = mock(TermMember.class);
        when(target.getTerm()).thenReturn(anotherTerm);
        when(termMemberRepository.findById(2L)).thenReturn(Optional.of(target));

        assertError(() -> service.getTermMemberCharges(1L, 2L, request(null, 0, 7)),
                ErrorCode.TERM_MEMBER_NOT_FOUND);
        verify(chargeSummaryService, never()).getTermMemberCharges(any(), any(), any(), any());
    }

    private TermMemberChargeListRequest request(
            ChargeMemberPaymentStatus status, int page, int size) {
        TermMemberChargeListRequest request = new TermMemberChargeListRequest();
        request.setPaymentStatus(status);
        request.setPage(page);
        request.setSize(size);
        return request;
    }

    private ChargeMember chargeMember(Long id, String title, Long amount, LocalDate dueDate,
                                      ChargeMemberPaymentStatus status) {
        Charge charge = mock(Charge.class);
        when(charge.getId()).thenReturn(id);
        when(charge.getTitle()).thenReturn(title);
        when(charge.getDueDate()).thenReturn(dueDate);
        ChargeMember member = mock(ChargeMember.class);
        when(member.getCharge()).thenReturn(charge);
        when(member.getAssignedAmount()).thenReturn(amount);
        when(member.getPaymentStatus()).thenReturn(status);
        return member;
    }

    private void assertError(Runnable operation, ErrorCode errorCode) {
        assertThatThrownBy(operation::run)
                .isInstanceOf(CustomException.class)
                .extracting(error -> ((CustomException) error).getErrorCode())
                .isEqualTo(errorCode);
    }
}
