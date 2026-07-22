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

    public Long calculateIncome(Long termId) {
        return transactionRepository.sumAmountByTermIdAndTransactionType(
                termId,
                TransactionType.INCOME
        );
    }
    public Long calculateExpense(Long termId) {
        return transactionRepository.sumAmountByTermIdAndTransactionType(
                termId,
                TransactionType.EXPENSE
        );
    }
}