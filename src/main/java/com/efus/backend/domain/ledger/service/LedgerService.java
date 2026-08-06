package com.efus.backend.domain.ledger.service;

import com.efus.backend.domain.charge.dto.internal.LedgerChargeDto;
import com.efus.backend.domain.charge.service.ChargeSummaryService;
import com.efus.backend.domain.ledger.dto.response.LedgerEntryListResponse;
import com.efus.backend.domain.ledger.dto.response.LedgerEntryResponse;
import com.efus.backend.domain.ledger.entity.LedgerFlowType;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.receipt.entity.Receipt;
import com.efus.backend.domain.receipt.service.ReceiptQueryService;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.entity.TransactionType;
import com.efus.backend.domain.transaction.repository.TransactionRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final TransactionRepository transactionRepository;
    private final ReceiptQueryService receiptQueryService;
    private final TermQueryService termQueryService;
    private final MemberQueryService memberQueryService;
    private final ChargeSummaryService chargeSummaryService;

    public LedgerEntryListResponse getLedgerEntries(
            Long termId,
            LocalDate fromDate,
            LocalDate toDate,
            LedgerFlowType type,
            boolean includeDeleted,
            Long fundingId,
            int page,
            int size
    ) {
        termQueryService.getTerm(termId);

        memberQueryService.validateTermMember(termId);

        validateDateRange(fromDate, toDate);

        TransactionType transactionType = resolveTransactionType(type);

        /*
         * Transaction과 Charge를 병합한 뒤 통합 페이지네이션을 수행해야 하므로
         * Transaction Repository 단계에서는 페이지네이션하지 않는다.
         */
        List<Transaction> transactions = transactionRepository.findLedgerTransactions(
                termId,
                fromDate,
                toDate,
                transactionType,
                includeDeleted,
                fundingId,
                Pageable.unpaged()
        ).getContent();

        List<Long> transactionIds = transactions.stream()
                .map(Transaction::getId)
                .toList();

        Map<Long, Receipt> receiptMap =
                receiptQueryService.getReceiptMapByTransactionIds(transactionIds);

        List<LedgerEntryResponse> entries = new ArrayList<>();

        entries.addAll(
                transactions.stream()
                        .map(transaction ->
                                LedgerEntryResponse.fromTransaction(transaction, receiptMap))
                        .toList()
        );

        /*
         * Charge는 totalIncome 계산에도 필요하므로 type과 관계없이 조회한다.
         *
         * 단, 목록에는 ALL 또는 INCOME일 때만 포함하고
         * EXPENSE 조회에서는 포함하지 않는다.
         */
        List<LedgerChargeDto> charges = chargeSummaryService.getLedgerCharges(
                termId,
                fromDate,
                toDate,
                includeDeleted,
                fundingId
        );

        if (type != LedgerFlowType.EXPENSE) {
            entries.addAll(
                    charges.stream()
                            .map(LedgerEntryResponse::fromCharge)
                            .toList()
            );
        }

        /*
         * Transaction은 transactionDate,
         * Charge는 dueDate가 LedgerEntryResponse.date에 들어간다.
         *
         * 통합 후 날짜 → 생성일 → ID 순으로 최신순 정렬한다.
         */
        entries.sort(
                Comparator.comparing(
                                LedgerEntryResponse::date,
                                Comparator.reverseOrder()
                        )
                        .thenComparing(
                                LedgerEntryResponse::createdAt,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                        .thenComparing(
                                LedgerEntryResponse::entryId,
                                Comparator.reverseOrder()
                        )
        );

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizeSize(size);

        int fromIndex = Math.min(
                normalizedPage * normalizedSize,
                entries.size()
        );

        int toIndex = Math.min(
                fromIndex + normalizedSize,
                entries.size()
        );

        List<LedgerEntryResponse> pagedEntries =
                entries.subList(fromIndex, toIndex);

        /*
         * 총수입:
         * 일반 INCOME 거래 + 실제 납부된 회비
         */
        Long totalIncome = transactionRepository.sumLedgerTransactionAmount(
                termId,
                fromDate,
                toDate,
                TransactionType.INCOME,
                includeDeleted,
                fundingId
        );

        /*
         * 총지출:
         * 일반 EXPENSE 거래
         */
        Long totalExpense = transactionRepository.sumLedgerTransactionAmount(
                termId,
                fromDate,
                toDate,
                TransactionType.EXPENSE,
                includeDeleted,
                fundingId
        );

        Long paidChargeAmount = charges.stream()
                .mapToLong(LedgerChargeDto::paidAmount)
                .sum();

        totalIncome += paidChargeAmount;

        return LedgerEntryListResponse.of(
                termId,
                totalIncome,
                totalExpense,
                pagedEntries,
                normalizedPage,
                normalizedSize,
                entries.size()
        );
    }

    private TransactionType resolveTransactionType(LedgerFlowType type) {
        if (type == LedgerFlowType.INCOME) {
            return TransactionType.INCOME;
        }

        if (type == LedgerFlowType.EXPENSE) {
            return TransactionType.EXPENSE;
        }

        return null;
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }
}