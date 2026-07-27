package com.efus.backend.domain.transaction.service;

import com.efus.backend.domain.transaction.entity.TransactionType;
import com.efus.backend.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionSummaryService {

    private final TransactionRepository transactionRepository;

    // TODO: funding,term 연관관계 + FundingQueryService,MemberQueryService,TermQueryService 병합 후 주석 해제
//    public Long calculateIncome(Long termId) {
//        return transactionRepository.sumAmountByTermIdAndTransactionType(
//                termId,
//                TransactionType.INCOME
//        );
//    }
    // TODO: funding,term 연관관계 + FundingQueryService,MemberQueryService,TermQueryService 병합 후 주석 해제
//    public Long calculateExpense(Long termId) {
//        return transactionRepository.sumAmountByTermIdAndTransactionType(
//                termId,
//                TransactionType.EXPENSE
//        );
//    }
}