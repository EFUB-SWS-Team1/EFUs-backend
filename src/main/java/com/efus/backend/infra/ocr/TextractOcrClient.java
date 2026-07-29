//OCR Infra Service
package com.efus.backend.infra.ocr;

import com.efus.backend.infra.s3.S3Properties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.S3Object;

@Component
@RequiredArgsConstructor
public class TextractOcrClient {

    private final TextractClient textractClient;
    private final S3Properties s3Properties;

    public List<String> extractLines(String storageKey) {
        DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                .document(Document.builder()
                        .s3Object(S3Object.builder()
                                .bucket(s3Properties.s3().bucket())
                                .name(storageKey)
                                .build())
                        .build())
                .build();

        DetectDocumentTextResponse response = textractClient.detectDocumentText(request);

        return response.blocks().stream()
                .filter(block -> block.blockType() == BlockType.LINE)
                .map(Block::text)
                .toList();
    }
}