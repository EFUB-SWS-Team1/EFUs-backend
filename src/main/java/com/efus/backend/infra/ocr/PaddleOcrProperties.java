package com.efus.backend.infra.ocr;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ocr.paddle")
public record PaddleOcrProperties(
        String baseUrl
) {
}