package com.efus.backend.domain.transaction.service;

import com.efus.backend.domain.transaction.dto.response.TransactionHistoryListResponse;
import com.efus.backend.domain.transaction.dto.response.TransactionHistoryResponse;
import com.efus.backend.domain.transaction.repository.TransactionHistoryRepository;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionHistoryService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private final TransactionHistoryRepository transactionHistoryRepository;

    // TODO: transaction 단건 조회 메서드 병합 후 주석 해제
    // private final TransactionQueryService transactionQueryService;

    // TODO: member QueryService 병합 후 주석 해제
    // private final MemberQueryService memberQueryService;

    public TransactionHistoryListResponse getTransactionHistories(
            Long transactionId,
            Integer page,
            Integer size
    ) {
        int pageNumber = page == null ? DEFAULT_PAGE : page;
        int pageSize = size == null ? DEFAULT_SIZE : size;

        validatePageRequest(pageNumber, pageSize);

        // TODO: term 연관관계 + TransactionQueryService 병합 후 주석 해제 (transactionId로 거래를 먼저 조회하고, 거래가 속한 termId를 가져와야 함)
        // Transaction transaction = transactionQueryService.getTransaction(transactionId);
        // Long termId = transaction.getTerm().getId();
        // TODO: member QueryService 병합 후 주석 해제 (조회 API이므로 STAFF/ACTIVE 검증은 하지 않고, 해당 기수 구성원 여부만 검증)
        // memberQueryService.validateTermMember(termId);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<TransactionHistoryResponse> historyPage = transactionHistoryRepository
                .findAllByTransaction_IdOrderByChangedAtDesc(transactionId, pageable)
                .map(TransactionHistoryResponse::from);

        return TransactionHistoryListResponse.from(historyPage);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }
}