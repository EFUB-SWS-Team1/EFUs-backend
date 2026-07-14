package com.efus.backend.infra.s3;

import com.efus.backend.global.response.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// S3 upload 테스트용
@RestController
@RequestMapping("/api/files")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping(value = "/receipts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<S3UploadResponse> uploadReceiptImage(
            @RequestPart("file") MultipartFile file
    ) {
        S3UploadResponse response = s3Service.uploadReceiptImage(file);
        return ApiResponse.success(response, "영수증 이미지 업로드가 완료되었습니다.");
    }

    @DeleteMapping
    public ApiResponse<Void> deleteFile(@RequestParam String storageKey) {
        s3Service.delete(storageKey);
        return ApiResponse.successWithMessage("파일 삭제가 완료되었습니다.");
    }
}