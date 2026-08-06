package com.efus.backend.infra.ocr;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PaddleOcrProperties.class)
public class PaddleOcrConfig {
}