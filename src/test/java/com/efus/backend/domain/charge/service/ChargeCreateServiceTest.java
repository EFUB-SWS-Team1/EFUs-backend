package com.efus.backend.domain.charge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.dto.request.ChargeCreateRequest;
import com.efus.backend.domain.charge.dto.response.ChargeCreateResponse;
import com.efus.backend.domain.charge.entity.Charge;
import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import com.efus.backend.domain.charge.entity.ChargeMethod;
import com.efus.backend.domain.charge.entity.ChargeTargetMode;
import com.efus.backend.domain.charge.repository.ChargeMemberRepository;
import com.efus.backend.domain.charge.repository.ChargeRepository;
import com.efus.backend.domain.funding.service.FundingQueryService;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ChargeCreateServiceTest {

    @Test
    void equalSplitPersistsEqualAmountsAndCalculatedRequestedAmount() {
        TermQueryService termQueryService = mock(TermQueryService.class);
        MemberQueryService memberQueryService = mock(MemberQueryService.class);
        FundingQueryService fundingQueryService = mock(FundingQueryService.class);
        ChargeRepository chargeRepository = mock(ChargeRepository.class);
        ChargeMemberRepository chargeMemberRepository = mock(ChargeMemberRepository.class);
        ChargeService service = new ChargeService(termQueryService, memberQueryService, fundingQueryService,
                chargeRepository, chargeMemberRepository);

        OrganizationTerm term = mock(OrganizationTerm.class);
        TermMember creator = mock(TermMember.class);
        List<TermMember> targets = List.of(member(1L), member(2L), member(3L));
        when(termQueryService.getTerm(1L)).thenReturn(term);
        when(memberQueryService.getCurrentActiveStaff(1L)).thenReturn(creator);
        when(memberQueryService.getSelectedActiveMembers(1L, List.of(1L, 2L, 3L))).thenReturn(targets);
        when(chargeRepository.save(any(Charge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChargeCreateResponse response = service.createCharge(1L, new ChargeCreateRequest(
                "8월 회비", ChargeMethod.EQUAL_SPLIT, LocalDate.of(2026, 8, 31), null, null,
                ChargeTargetMode.SELECTED, List.of(1L, 2L, 3L), null, 10_000L));

        ArgumentCaptor<Charge> chargeCaptor = ArgumentCaptor.forClass(Charge.class);
        verify(chargeRepository).save(chargeCaptor.capture());
        assertThat(chargeCaptor.getValue().getRequestedAmount()).isEqualTo(9_999L);
        assertThat(response.requestedAmount()).isEqualTo(9_999L);
        assertThat(response.targetCount()).isEqualTo(3);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ChargeMember>> membersCaptor = ArgumentCaptor.forClass(List.class);
        verify(chargeMemberRepository).saveAll(membersCaptor.capture());
        assertThat(membersCaptor.getValue()).hasSize(3);
        assertThat(membersCaptor.getValue()).extracting(ChargeMember::getAssignedAmount).containsOnly(3_333L);
        assertThat(membersCaptor.getValue()).extracting(ChargeMember::getPaymentStatus)
                .containsOnly(ChargeMemberPaymentStatus.UNPAID);
        assertThat(membersCaptor.getValue()).extracting(ChargeMember::getPaidAt).containsOnlyNulls();
    }

    private TermMember member(Long id) {
        TermMember member = mock(TermMember.class);
        when(member.getId()).thenReturn(id);
        return member;
    }
}
