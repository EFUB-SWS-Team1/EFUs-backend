package com.efus.backend.infra.s3;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class S3Service {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final S3Client s3Client;
    private final S3Properties properties;

    public S3Service(S3Client s3Client, S3Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    // 영수증 이미지 파일을 검증한 뒤 S3에 업로드하고, 저장된 파일 정보를 반환
    public S3UploadResponse uploadReceiptImage(MultipartFile file) {
        validateImageFile(file);

        String storageKey = createReceiptStorageKey(file);
        String bucket = properties.s3().bucket();
        String contentType = file.getContentType();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();
        try {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read upload file.", e);
        }

        String imageUrl = s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                        .bucket(bucket)
                        .key(storageKey)
                        .build())
                .toString();

        return new S3UploadResponse(
                imageUrl,
                storageKey,
                file.getOriginalFilename(),
                file.getSize(),
                contentType
        );
    }

    // S3에 저장된 파일을 storageKey 기준으로 삭제
    public void delete(String storageKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(properties.s3().bucket())
                .key(storageKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    // 업로드 파일이 비어 있지 않은지, 크기와 이미지 타입이 허용 범위인지 검증
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Upload file is empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must be 10MB or less.");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only jpeg, png, and webp images are allowed.");
        }
    }

    // 영수증 이미지를 S3에 저장할 고유한 storageKey를 생성한다.
    private String createReceiptStorageKey(MultipartFile file) {
        String extension = getExtension(file.getContentType());
        return "receipts/temp/" + UUID.randomUUID() + "." + extension;
    }

    // contentType에 맞는 파일 확장자를 반환
    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported image type.");
        };
    }
}