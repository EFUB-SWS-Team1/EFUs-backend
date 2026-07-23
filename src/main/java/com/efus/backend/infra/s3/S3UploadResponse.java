package com.efus.backend.infra.s3;


// S3 업로드가 끝난 후 클라이언트에게 돌려줄 파일 정보
public record S3UploadResponse(
        String imageUrl,
        String storageKey,
        String originalFileName,
        long fileSize,
        String contentType
) {
}