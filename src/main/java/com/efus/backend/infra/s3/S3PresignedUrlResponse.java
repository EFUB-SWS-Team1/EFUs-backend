package com.efus.backend.infra.s3;


// 클라이언트가 S3에 직접 업로드할 때 사용할 presigned URL 정보
public record S3PresignedUrlResponse(
        String presignedUrl,
        String storageKey,
        String originalFilename,
        Long fileSize,
        String contentType
) {
}