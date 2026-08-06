package com.efus.backend.domain.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.efus.backend.domain.charge.dto.internal.LedgerChargeDto;
import com.efus.backend.domain.charge.service.ChargeSummaryService;
import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.ledger.dto.response.LedgerEntryListResponse;
import com.efus.backend.domain.ledger.entity.LedgerEntryType;
import com.efus.backend.domain.ledger.entity.LedgerFlowType;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.receipt.service.ReceiptQueryService;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.entity.TransactionType;
import com.efus.backend.domain.transaction.repository.TransactionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class LedgerServiceTest {

    private TransactionRepository transactionRepository;
    private ChargeSummaryService chargeSummaryService;
    private MemberQueryService memberQueryService;
    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        ReceiptQueryService receiptQueryService = mock(ReceiptQueryService.class);
        TermQueryService termQueryService = mock(TermQueryService.class);
        memberQueryService = mock(MemberQueryService.class);
        chargeSummaryService = mock(ChargeSummaryService.class);
        ledgerService = new LedgerService(
                transactionRepository,
                receiptQueryService,
                termQueryService,
                memberQueryService,
                chargeSummaryService
        );
        when(receiptQueryService.getReceiptMapByTransactionIds(any())).thenReturn(Map.of());
        when(transactionRepository.sumLedgerTransactionAmount(
                any(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(0L);
    }

    @Test
    void mergesFundingTransactionsAndChargesThenSortsAndPaginates() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 31);
        Funding funding = mock(Funding.class);
        when(funding.getId()).thenReturn(7L);
        when(funding.getName()).thenReturn("행사");
        Transaction transaction = mock(Transaction.class);
        when(transaction.getId()).thenReturn(2L);
        when(transaction.getFunding()).thenReturn(funding);
        when(transaction.getTransactionType()).thenReturn(TransactionType.EXPENSE);
        when(transaction.getTitle()).thenReturn("지출");
        when(transaction.getAmount()).thenReturn(10_000L);
        when(transaction.getTransactionDate()).thenReturn(LocalDate.of(2026, 8, 10));
        LedgerChargeDto charge = new LedgerChargeDto(
                3L, "회비", LocalDate.of(2026, 8, 20),
                30_000L, 20_000L, 7L, "행사", null, false,
                LocalDateTime.of(2026, 8, 1, 12, 0));
        when(transactionRepository.findLedgerTransactions(
                1L, from, to, null, false, 7L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(transaction)));
        when(chargeSummaryService.getLedgerCharges(1L, from, to, false, 7L))
                .thenReturn(List.of(charge));

        LedgerEntryListResponse result = ledgerService.getLedgerEntries(
                1L, from, to, LedgerFlowType.ALL, false, 7L, 0, 1);

        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().get(0).entryType()).isEqualTo(LedgerEntryType.CHARGE);
        assertThat(result.entries().get(0).paidAmount()).isEqualTo(20_000L);
        assertThat(result.pageInfo().totalElements()).isEqualTo(2);
        assertThat(result.pageInfo().hasNext()).isTrue();
        verify(memberQueryService).validateTermMember(1L);
        verify(chargeSummaryService).getLedgerCharges(1L, from, to, false, 7L);
    }

    @Test
    void expenseFilterExcludesCharges() {
        when(transactionRepository.findLedgerTransactions(
                1L, null, null, TransactionType.EXPENSE, true, 7L, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of()));

        LedgerEntryListResponse result = ledgerService.getLedgerEntries(
                1L, null, null, LedgerFlowType.EXPENSE, true, 7L, 0, 20);

        assertThat(result.entries()).isEmpty();
        assertThat(result.pageInfo().totalElements()).isZero();
    }
}
