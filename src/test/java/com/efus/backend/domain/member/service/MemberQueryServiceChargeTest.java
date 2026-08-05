package com.efus.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemberQueryServiceChargeTest {

    private TermMemberRepository termMemberRepository;
    private CurrentUserService currentUserService;
    private MemberQueryService memberQueryService;

    @BeforeEach
    void setUp() {
        termMemberRepository = mock(TermMemberRepository.class);
        currentUserService = mock(CurrentUserService.class);
        memberQueryService = new MemberQueryService(
                currentUserService,
                mock(TermQueryService.class),
                termMemberRepository
        );
    }

    @Test
    void activeStaffValidationRejectsMemberRole() {
        TermMember member = currentMember(false, true);

        assertError(() -> memberQueryService.validateActiveStaff(1L), ErrorCode.STAFF_REQUIRED);
    }

    @Test
    void activeStaffValidationRejectsInactiveStaff() {
        TermMember member = currentMember(true, false);

        assertError(() -> memberQueryService.validateActiveStaff(1L), ErrorCode.INACTIVE_TERM_MEMBER);
    }

    @Test
    void selectedTargetsMustAllExistInTerm() {
        when(termMemberRepository.findAllByTermIdAndIdIn(1L, List.of(1L, 2L)))
                .thenReturn(List.of(mock(TermMember.class)));

        assertError(
                () -> memberQueryService.getSelectedActiveMembers(1L, List.of(1L, 2L)),
                ErrorCode.INVALID_CHARGE_TARGET
        );
    }

    @Test
    void selectedTargetsMustAllBeActive() {
        TermMember active = mock(TermMember.class);
        TermMember inactive = mock(TermMember.class);
        when(active.isActive()).thenReturn(true);
        when(inactive.isActive()).thenReturn(false);
        when(termMemberRepository.findAllByTermIdAndIdIn(1L, List.of(1L, 2L)))
                .thenReturn(List.of(active, inactive));

        assertError(
                () -> memberQueryService.getSelectedActiveMembers(1L, List.of(1L, 2L)),
                ErrorCode.INACTIVE_CHARGE_TARGET
        );
    }

    private void assertError(Runnable operation, ErrorCode errorCode) {
        assertThatThrownBy(operation::run)
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(errorCode);
    }

    private TermMember currentMember(boolean staff, boolean active) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(10L);
        when(currentUserService.getCurrentUser()).thenReturn(user);

        TermMember member = mock(TermMember.class);
        when(member.isStaff()).thenReturn(staff);
        when(member.isActive()).thenReturn(active);
        when(termMemberRepository.findByTermIdAndUserId(1L, 10L))
                .thenReturn(java.util.Optional.of(member));
        return member;
    }
}
