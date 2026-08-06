package com.efus.backend.infra.ocr;

import com.efus.backend.infra.s3.S3Properties;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Component
@RequiredArgsConstructor
public class PaddleOcrClient {

    private final PaddleOcrProperties properties;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public List<String> extractLines(String storageKey) {
        byte[] imageBytes = readImageBytes(storageKey);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "receipt.png";
            }
        }).contentType(MediaType.IMAGE_PNG);

        PaddleOcrResponse response = WebClient.create(properties.baseUrl())
                .post()
                .uri("/ocr/receipt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(PaddleOcrResponse.class)
                .block();

        if (response == null || response.lines() == null) {
            return List.of();
        }

        return response.lines().stream()
                .map(PaddleOcrLine::text)
                .filter(text -> text != null && !text.isBlank())
                .toList();
    }

    private byte[] readImageBytes(String storageKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.s3().bucket())
                .key(storageKey)
                .build();

        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
        return response.asByteArray();
    }

    private record PaddleOcrResponse(List<PaddleOcrLine> lines) {
    }

    private record PaddleOcrLine(String text, Double confidence, Object box) {
    }
}