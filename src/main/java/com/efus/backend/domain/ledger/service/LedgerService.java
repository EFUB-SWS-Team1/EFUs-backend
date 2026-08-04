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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // TODO: Charge 도메인 병합 후 주석 해제
//     private final ChargeQueryService chargeQueryService;

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

        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(
                        Sort.Order.desc("transactionDate"),
                        Sort.Order.desc("id")
                )
        );

        TransactionType transactionType = resolveTransactionType(type);

        Page<Transaction> transactionPage = transactionRepository.findLedgerTransactions(
                termId,
                fromDate,
                toDate,
                transactionType,
                includeDeleted,
                fundingId,
                pageRequest
        );

        List<Long> transactionIds = transactionPage.getContent()
                .stream()
                .map(Transaction::getId)
                .toList();

        Map<Long, Receipt> receiptMap = receiptQueryService.getReceiptMapByTransactionIds(transactionIds);

        List<LedgerEntryResponse> entries = transactionPage.getContent()
                .stream()
                .map(transaction -> LedgerEntryResponse.fromTransaction(transaction, receiptMap))
                .toList();

        // TODO: Charge 도메인 병합 후 주석 해제
        //  type이 ALL 또는 INCOME인 경우 회비 청구도 목록에 포함해야 합니다.
        //  CHARGE 날짜 필터 기준은 dueDate입니다.
//         List<LedgerEntryResponse> chargeEntries = chargeQueryService.getLedgerCharges(
//                 termId,
//                 fromDate,
//                 toDate,
//                 includeDeleted,
//                 fundingId,
//                 pageRequest
//         ).stream()
//                 .map(LedgerEntryResponse::fromCharge)
//                 .toList();

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

        // TODO: Charge 도메인 병합 후 실제 납부 완료된 회비 금액을 totalIncome에 더해야 합니다.
        //  문서 기준: 회비 청구는 목록에서는 INCOME 성격이지만, 실제 총수입에는 납부된 금액만 반영합니다.
//         totalIncome += chargeQueryService.sumPaidChargeAmount(termId, fromDate, toDate, includeDeleted,fundingId);

        return LedgerEntryListResponse.of(
                termId,
                totalIncome,
                totalExpense,
                entries,
                transactionPage
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