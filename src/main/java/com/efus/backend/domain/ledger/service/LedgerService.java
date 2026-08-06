package com.efus.backend.domain.ledger.service;

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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.efus.backend.domain.charge.dto.internal.LedgerChargeDto;
import com.efus.backend.domain.charge.service.ChargeSummaryService;
import java.util.ArrayList;
import java.util.Comparator;

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

        List<Transaction> transactions = transactionRepository.findLedgerTransactionsForEntries(
                termId,
                fromDate,
                toDate,
                transactionType,
                includeDeleted,
                fundingId
        );

        List<Long> transactionIds = transactions.stream()
                .map(Transaction::getId)
                .toList();

        Map<Long, Receipt> receiptMap = receiptQueryService.getReceiptMapByTransactionIds(transactionIds);

        List<LedgerEntryResponse> entries = new ArrayList<>();

        entries.addAll(
                transactions.stream()
                        .map(transaction -> LedgerEntryResponse.fromTransaction(transaction, receiptMap))
                        .toList()
        );

        List<LedgerChargeDto> charges = List.of();

        if (type == LedgerFlowType.ALL || type == LedgerFlowType.INCOME) {
            charges = chargeSummaryService.getLedgerCharges(
                    termId,
                    fromDate,
                    toDate,
                    includeDeleted,
                    fundingId
            );

            entries.addAll(
                    charges.stream()
                            .map(LedgerEntryResponse::fromCharge)
                            .toList()
            );
        }

        entries.sort(
                Comparator.comparing(LedgerEntryResponse::date)
                        .reversed()
                        .thenComparing(LedgerEntryResponse::createdAt, Comparator.reverseOrder())
                        .thenComparing(LedgerEntryResponse::entryId, Comparator.reverseOrder())
        );

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizeSize(size);
        int fromIndex = Math.min(normalizedPage * normalizedSize, entries.size());
        int toIndex = Math.min(fromIndex + normalizedSize, entries.size());

        List<LedgerEntryResponse> pagedEntries = entries.subList(fromIndex, toIndex);

        Long totalIncome = transactionRepository.sumLedgerTransactionAmount(
                termId,
                fromDate,
                toDate,
                TransactionType.INCOME,
                includeDeleted,
                fundingId
        );

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