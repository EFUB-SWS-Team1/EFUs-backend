package com.efus.backend.domain.transaction.service;

import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.repository.TransactionRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;

    // TODO: funding,term 연관관계 + FundingQueryService,MemberQueryService,TermQueryService 병합 후 주석 해제
//    public Transaction getTransactionInTerm(Long termId, Long transactionId) {
//        return transactionRepository.findByIdAndTerm_Id(transactionId, termId)
//                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));
//    }

    // TODO: funding,term 연관관계 + FundingQueryService,MemberQueryService,TermQueryService 병합 후 주석 해제
//    public Transaction getActiveTransactionInTerm(Long termId, Long transactionId) {
//        Transaction transaction = getTransactionInTerm(termId, transactionId);
//
//        if (transaction.isDeleted()) {
//            throw new CustomException(ErrorCode.TRANSACTION_ALREADY_DELETED);
//        }
//
//        return transaction;
//    }
}