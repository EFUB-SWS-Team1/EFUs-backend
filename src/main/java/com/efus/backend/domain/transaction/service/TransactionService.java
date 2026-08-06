package com.efus.backend.domain.transaction.service;

import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.funding.service.FundingQueryService;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.transaction.dto.internal.TransactionSnapshot;
import com.efus.backend.domain.transaction.dto.request.TransactionCreateRequest;
import com.efus.backend.domain.transaction.dto.request.TransactionUpdateRequest;
import com.efus.backend.domain.transaction.dto.response.TransactionDetailResponse;
import com.efus.backend.domain.transaction.dto.response.TransactionResponse;
import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.entity.TransactionActionType;
import com.efus.backend.domain.transaction.entity.TransactionHistory;
import com.efus.backend.domain.transaction.repository.TransactionHistoryRepository;
import com.efus.backend.domain.transaction.repository.TransactionRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private static final String UPDATE_SUMMARY = "거래 정보가 수정되었습니다.";
    private static final String DELETE_SUMMARY = "거래가 삭제되었습니다.";

    private final TransactionRepository transactionRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final TransactionQueryService transactionQueryService;
    private final TermQueryService termQueryService;
    private final MemberQueryService memberQueryService;
    private final FundingQueryService fundingQueryService;
    private final ObjectMapper objectMapper;

    public TransactionResponse createTransaction(Long termId, TransactionCreateRequest request) {
        OrganizationTerm term = termQueryService.getTerm(termId);
        TermMember currentTermMember = memberQueryService.getCurrentTermMember(termId);
        memberQueryService.validateStaff(termId);

        termQueryService.validateActiveTerm(termId);

        Funding funding = getFundingOrNull(termId, request.fundingId());

        Transaction transaction = Transaction.builder()
                .term(term)
                .funding(funding)
                .createdByTermMember(currentTermMember)
                .transactionType(request.transactionType())
                .title(request.title())
                .amount(request.amount())
                .transactionDate(request.transactionDate())
                .memo(request.memo())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.from(savedTransaction);
    }

    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransaction(Long termId, Long transactionId) {
        Transaction transaction = transactionQueryService.getActiveTransactionInTerm(termId, transactionId);
        memberQueryService.validateTermMember(termId);
        return TransactionDetailResponse.from(transaction);
    }

    public TransactionResponse updateTransaction(
            Long termId,
            Long transactionId,
            TransactionUpdateRequest request
    ) {
        Transaction transaction = transactionQueryService.getActiveTransactionInTerm(termId, transactionId);

        TermMember currentTermMember = memberQueryService.getCurrentTermMember(termId);
        memberQueryService.validateStaff(termId);

        termQueryService.validateActiveTerm(termId);

        TransactionSnapshot beforeSnapshot = TransactionSnapshot.from(transaction);

        Funding funding = resolveFundingForUpdate(termId, transaction, request);

        transaction.update(
                funding,
                currentTermMember,
                request.getTransactionType() == null ? transaction.getTransactionType() : request.getTransactionType(),
                request.getTitle() == null ? transaction.getTitle() : request.getTitle(),
                request.getAmount() == null ? transaction.getAmount() : request.getAmount(),
                request.getTransactionDate() == null ? transaction.getTransactionDate() : request.getTransactionDate(),
                resolveMemoForUpdate(transaction, request)
        );

        TransactionSnapshot afterSnapshot = TransactionSnapshot.from(transaction);

        saveHistory(
                transaction,
                currentTermMember,
                TransactionActionType.UPDATE,
                UPDATE_SUMMARY,
                beforeSnapshot,
                afterSnapshot
        );
        return TransactionResponse.from(transaction);
 }

    public void deleteTransaction(Long termId, Long transactionId) {
        Transaction transaction = transactionQueryService.getActiveTransactionInTerm(termId, transactionId);

        TermMember currentTermMember = memberQueryService.getCurrentTermMember(termId);
        memberQueryService.validateStaff(termId);

        termQueryService.validateActiveTerm(termId);

        TransactionSnapshot beforeSnapshot = TransactionSnapshot.from(transaction);

        transaction.softDelete(currentTermMember, LocalDateTime.now());
        saveHistory(
                transaction,
                currentTermMember,
                TransactionActionType.DELETE,
                DELETE_SUMMARY,
                beforeSnapshot,
                null
        );
    }

    private Funding getFundingOrNull(Long termId, Long fundingId) {
        if (fundingId == null) {
            return null;
        }
        return fundingQueryService.getFundingInTerm(termId, fundingId);
    }

    private Funding resolveFundingForUpdate(
            Long termId,
            Transaction transaction,
            TransactionUpdateRequest request
    ) {
        if (!request.hasFundingId()) {
            return transaction.getFunding();
        }

        if (request.isFundingIdNull()) {
            return null;
        }

        return fundingQueryService.getFundingInTerm(termId, request.getFundingIdValue());
    }

    private String resolveMemoForUpdate(Transaction transaction, TransactionUpdateRequest request) {
        if (!request.hasMemo()) {
            return transaction.getMemo();
        }

        if (request.isMemoNull()) {
            return null;
        }

        return request.getMemoValue();
    }

    private void saveHistory(
            Transaction transaction,
            TermMember actor,
            TransactionActionType actionType,
            String summary,
            TransactionSnapshot beforeSnapshot,
            TransactionSnapshot afterSnapshot
    ) {
        TransactionHistory history = TransactionHistory.builder()
                .transaction(transaction)
                .actorTermMember(actor)
                .actionType(actionType)
                .summary(summary)
                .beforeData(toJson(beforeSnapshot))
                .afterData(afterSnapshot == null ? null : toJson(afterSnapshot))
                .changedAt(LocalDateTime.now())
                .build();

        transactionHistoryRepository.save(history);
    }

    private String toJson(TransactionSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.TRANSACTION_HISTORY_SERIALIZATION_FAILED);
        }
    }
}
