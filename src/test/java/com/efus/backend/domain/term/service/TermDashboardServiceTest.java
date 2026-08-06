package com.efus.backend.domain.term.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.service.ChargeSummaryService;
import com.efus.backend.domain.funding.service.FundingQueryService;
import com.efus.backend.domain.invitation.service.InvitationCommandService;
import com.efus.backend.domain.ledger.dto.response.LedgerEntryListResponse;
import com.efus.backend.domain.ledger.service.LedgerService;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.organization.repository.OrganizationRepository;
import com.efus.backend.domain.term.dto.response.TermDashboardResponse;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.term.repository.TermRepository;
import com.efus.backend.domain.transaction.service.TransactionSummaryService;
import com.efus.backend.domain.user.service.CurrentUserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TermDashboardServiceTest {

    private TermQueryService termQueryService;
    private MemberQueryService memberQueryService;
    private TransactionSummaryService transactionSummaryService;
    private ChargeSummaryService chargeSummaryService;
    private LedgerService ledgerService;
    private FundingQueryService fundingQueryService;
    private TermService termService;

    @BeforeEach
    void setUp() {
        termQueryService = mock(TermQueryService.class);
        memberQueryService = mock(MemberQueryService.class);
        transactionSummaryService = mock(TransactionSummaryService.class);
        chargeSummaryService = mock(ChargeSummaryService.class);
        ledgerService = mock(LedgerService.class);
        fundingQueryService = mock(FundingQueryService.class);
        termService = new TermService(
                mock(CurrentUserService.class), mock(TermRepository.class),
                mock(TermMemberRepository.class), mock(OrganizationRepository.class),
                mock(InvitationCommandService.class), termQueryService, memberQueryService,
                transactionSummaryService, chargeSummaryService, ledgerService,
                fundingQueryService);

        when(transactionSummaryService.calculateIncome(any())).thenReturn(0L);
        when(transactionSummaryService.calculateExpense(any())).thenReturn(0L);
        when(chargeSummaryService.calculatePaidAmount(any())).thenReturn(0L);
        when(ledgerService.getLedgerEntries(any(), any(), any(), any(), anyBoolean(), any(), anyInt(), anyInt()))
                .thenReturn(LedgerEntryListResponse.of(1L, 0L, 0L, List.of(), 0, 20, 0));
        when(fundingQueryService.getFundingSpendings(any())).thenReturn(List.of());
    }

    @Test
    void activeStaffCanReadDashboard() {
        stubAccess(TermStatus.ACTIVE, TermMemberRole.STAFF);

        TermDashboardResponse result = termService.getTermDashboard(1L);

        assertThat(result.term().status()).isEqualTo("ACTIVE");
        assertThat(result.term().myRole()).isEqualTo("STAFF");
    }

    @Test
    void activeMemberCanReadDashboard() {
        stubAccess(TermStatus.ACTIVE, TermMemberRole.MEMBER);

        assertThat(termService.getTermDashboard(1L).term().myRole()).isEqualTo("MEMBER");
    }

    @Test
    void closedTermCanBeReadAndFinancialServicesAreCombined() {
        stubAccess(TermStatus.CLOSED, TermMemberRole.STAFF);
        when(transactionSummaryService.calculateIncome(1L)).thenReturn(5_000L);
        when(chargeSummaryService.calculatePaidAmount(1L)).thenReturn(2_000L);
        when(transactionSummaryService.calculateExpense(1L)).thenReturn(3_500L);

        TermDashboardResponse result = termService.getTermDashboard(1L);

        assertThat(result.term().status()).isEqualTo("CLOSED");
        assertThat(result.financialSummary().totalIncome()).isEqualTo(7_000L);
        assertThat(result.financialSummary().totalExpense()).isEqualTo(3_500L);
        assertThat(result.financialSummary().balance()).isEqualTo(3_500L);
        assertThat(result.financialSummary().usageRate()).isEqualTo(50.0);
    }

    @Test
    void emptyDashboardUsesZerosAndEmptyLists() {
        stubAccess(TermStatus.ACTIVE, TermMemberRole.MEMBER);

        TermDashboardResponse result = termService.getTermDashboard(1L);

        assertThat(result.financialSummary().usageRate()).isZero();
        assertThat(result.recentLedgerEntries()).isEmpty();
        assertThat(result.fundingBudgets()).isEmpty();
    }

    private void stubAccess(TermStatus status, TermMemberRole role) {
        Organization organization = mock(Organization.class);
        when(organization.getId()).thenReturn(10L);
        when(organization.getName()).thenReturn("EFUB");
        OrganizationTerm term = mock(OrganizationTerm.class);
        when(term.getId()).thenReturn(1L);
        when(term.getOrganization()).thenReturn(organization);
        when(term.getName()).thenReturn("6기");
        when(term.getTermStatus()).thenReturn(status);
        TermMember member = mock(TermMember.class);
        when(member.getRole()).thenReturn(role);
        when(termQueryService.getTerm(1L)).thenReturn(term);
        when(memberQueryService.getDashboardMember(1L, 10L)).thenReturn(member);
    }
}
