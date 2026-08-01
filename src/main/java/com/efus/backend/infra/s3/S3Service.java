package com.efus.backend.infra.s3;

import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3Service {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(10);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    public S3Service(S3Client s3Client, S3Presigner s3Presigner, S3Properties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    public S3PresignedUrlResponse generateReceiptUploadUrl(
            Long transactionId,
            String originalFilename,
            String contentType,
            Long fileSize
    ) {
        validateReceiptFile(originalFilename, contentType, fileSize);

        String storageKey = createReceiptStorageKey(transactionId, contentType);
        String presignedUrl = generateUploadPresignedUrl(storageKey, contentType);

        return new S3PresignedUrlResponse(
                presignedUrl,
                storageKey,
                originalFilename,
                fileSize,
                contentType
        );
    }

    public String generateReadPresignedUrl(String storageKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.s3().bucket())
                .key(storageKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_EXPIRATION)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    public void delete(String storageKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(properties.s3().bucket())
                .key(storageKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    private String generateUploadPresignedUrl(String storageKey, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.s3().bucket())
                .key(storageKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGNED_URL_EXPIRATION)
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest)
                .url()
                .toString();
    }

    private void validateReceiptFile(String originalFilename, String contentType, Long fileSize) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_RECEIPT_FILE);
        }

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new CustomException(ErrorCode.INVALID_RECEIPT_FILE);
        }

        if (fileSize == null || fileSize <= 0 || fileSize > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_RECEIPT_FILE);
        }
    }

    private String createReceiptStorageKey(Long transactionId, String contentType) {
        String extension = getExtension(contentType);
        return "receipts/" + transactionId + "/" + UUID.randomUUID() + "." + extension;
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new CustomException(ErrorCode.INVALID_RECEIPT_FILE);
        };
    }
}