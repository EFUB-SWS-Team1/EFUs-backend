package com.efus.backend.domain.receipt.service;

import com.efus.backend.domain.receipt.dto.response.ReceiptResponse;
import com.efus.backend.domain.receipt.entity.Receipt;
import com.efus.backend.domain.receipt.repository.ReceiptRepository;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.service.TransactionQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.efus.backend.infra.s3.S3Service;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.receipt.dto.request.ReceiptRequest;
import com.efus.backend.infra.s3.S3PresignedUrlResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptService {
    private final ReceiptRepository receiptRepository;
    private final TransactionQueryService transactionQueryService;
    private final S3Service s3Service;

    // TODO: MemberQueryService 병합 후 주석 해제
    // private final MemberQueryService memberQueryService;

    private final TermQueryService termQueryService;

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

    @Transactional
    public ReceiptResponse createOrReplaceReceiptUploadUrl(
            Long transactionId,
            ReceiptRequest request
    ) {
        Transaction transaction = transactionQueryService.getTransaction(transactionId);

        Long termId = transaction.getTerm().getId();

        // TODO: MemberQueryService 병합 후 주석 해제
        // TermMember currentTermMember = memberQueryService.getCurrentTermMember(termId);
        // memberQueryService.validateStaff(termId);

         termQueryService.validateActiveTerm(termId);

        if (transaction.isDeleted()) {
            throw new CustomException(ErrorCode.TRANSACTION_ALREADY_DELETED);
        }


        S3PresignedUrlResponse s3Response = s3Service.generateReceiptUploadUrl(
                transactionId,
                request.originalFilename(),
                request.contentType(),
                request.fileSize()
        );

        // TODO: MemberQueryService 병합 후 주석 해제
//        Receipt receipt = receiptRepository.findByTransaction_Id(transactionId)
//                .map(existingReceipt -> {
//                    existingReceipt.replaceFile(
//                            currentTermMember,
//                            s3Response.storageKey(),
//                            s3Response.presignedUrl(),
//                            s3Response.originalFilename(),
//                            s3Response.contentType(),
//                            s3Response.fileSize()
//                    );
//                    return existingReceipt;
//                })
//                .orElseGet(() -> receiptRepository.save(
//                        Receipt.builder()
//                                .transaction(transaction)
//                                .uploadedByTermMember(currentTermMember)
//                                .storageKey(s3Response.storageKey())
//                                .presignedUrl(s3Response.presignedUrl())
//                                .originalFilename(s3Response.originalFilename())
//                                .contentType(s3Response.contentType())
//                                .fileSize(s3Response.fileSize())
//                                .build()
//                ));

//        return ReceiptResponse.from(receipt, s3Response.presignedUrl());
        return null;
    }

}