package com.efus.backend.infra.s3;

import com.efus.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class S3Controller {
    private final S3Service s3Service;

    @PostMapping("/receipts/{transactionId}/presigned-url")
    public ApiResponse<S3PresignedUrlResponse> generateReceiptUploadUrl(
            @PathVariable Long transactionId,
            @RequestParam String originalFilename,
            @RequestParam String contentType,
            @RequestParam Long fileSize
    ) {
        S3PresignedUrlResponse response = s3Service.generateReceiptUploadUrl(
                transactionId,
                originalFilename,
                contentType,
                fileSize
        );

        return ApiResponse.success(response, "영수증 업로드 URL이 생성되었습니다.");
    }

    @GetMapping("/presigned-url")
    public ApiResponse<String> generateReadUrl(
            @RequestParam String storageKey
    ) {
        String presignedUrl = s3Service.generateReadPresignedUrl(storageKey);

        return ApiResponse.success(presignedUrl, "파일 조회 URL이 생성되었습니다.");
    }

    @DeleteMapping
    public ApiResponse<Void> deleteFile(@RequestParam String storageKey) {
        s3Service.delete(storageKey);

        return ApiResponse.successWithMessage("파일 삭제가 완료되었습니다.");
    }
}
