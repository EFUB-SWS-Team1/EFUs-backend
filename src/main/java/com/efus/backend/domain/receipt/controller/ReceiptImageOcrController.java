package com.efus.backend.domain.receipt.controller;

import com.efus.backend.domain.receipt.dto.response.ReceiptImageOcrResponse;
import com.efus.backend.domain.receipt.service.ReceiptOcrService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.global.response.ApiResponse;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receipts")
public class ReceiptImageOcrController {

    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE
    );

    private final ReceiptOcrService receiptOcrService;

    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReceiptImageOcrResponse>> recognizeReceiptAmount(
            @RequestPart("file") MultipartFile file
    ) {
        validate(file);

        try {
            Long amount = receiptOcrService.recognizeAmount(
                    file.getBytes(),
                    file.getOriginalFilename() == null ? "receipt" : file.getOriginalFilename(),
                    file.getContentType()
            );

            return ResponseEntity.ok(ApiResponse.success(
                    new ReceiptImageOcrResponse(amount),
                    "OCR 금액 인식이 완료되었습니다."
            ));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_RECEIPT_FILE);
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty() || !SUPPORTED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new CustomException(ErrorCode.INVALID_RECEIPT_FILE);
        }
    }
}
