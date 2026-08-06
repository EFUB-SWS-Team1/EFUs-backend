package com.efus.backend.domain.receipt.service;

import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.infra.ocr.PaddleOcrClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultReceiptOcrService implements ReceiptOcrService {

    private final PaddleOcrClient paddleOcrClient;
    private final ReceiptAmountParser receiptAmountParser;

    @Override
    public Long recognizeAmount(String storageKey) {
        try {
            List<String> lines = paddleOcrClient.extractLines(storageKey);

            return parseAmount(lines);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.RECEIPT_OCR_FAILED);
        }
    }

    @Override
    public Long recognizeAmount(byte[] imageBytes, String filename, String contentType) {
        try {
            List<String> lines = paddleOcrClient.extractLines(imageBytes, filename, contentType);

            return parseAmount(lines);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.RECEIPT_OCR_FAILED);
        }
    }

    private Long parseAmount(List<String> lines) {
        return receiptAmountParser.parseAmount(lines)
                .orElseThrow(() -> new CustomException(ErrorCode.RECEIPT_OCR_FAILED));
    }
}
