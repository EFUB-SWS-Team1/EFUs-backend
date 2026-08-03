//Temporary OCR Implementation

package com.efus.backend.domain.receipt.service;

import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class DefaultReceiptOcrService implements ReceiptOcrService {

    @Override
    public Long recognizeAmount(String storageKey) {
        // TODO: OCR Provider 연동 후 storageKey 기준 영수증 이미지에서 금액 인식 로직 구현
        throw new CustomException(ErrorCode.RECEIPT_OCR_FAILED);
    }
}