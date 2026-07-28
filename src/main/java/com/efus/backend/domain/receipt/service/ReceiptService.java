package com.efus.backend.domain.receipt.service;

import com.efus.backend.domain.receipt.dto.response.ReceiptResponse;
import com.efus.backend.domain.receipt.entity.Receipt;
import com.efus.backend.domain.receipt.repository.ReceiptRepository;
import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.service.TransactionQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.efus.backend.infra.s3.S3Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final TransactionQueryService transactionQueryService;
    private final S3Service s3Service;

    // TODO: MemberQueryService 병합 후 주석 해제
    // private final MemberQueryService memberQueryService;

    public ReceiptResponse getReceipt(Long transactionId) {
        Transaction transaction = transactionQueryService.getTransaction(transactionId);

        Long termId = transaction.getTerm().getId();

        // TODO: MemberQueryService 병합 후 주석 해제
        // memberQueryService.validateTermMember(termId);

        Receipt receipt = receiptRepository.findByTransaction_Id(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECEIPT_NOT_FOUND));

        String presignedUrl = s3Service.generateReadPresignedUrl(receipt.getStorageKey());
        return ReceiptResponse.from(receipt, presignedUrl);
    }
}