package com.efus.backend.infra.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

// application.yml에 있는 S3 설정 정보를 자바 코드에서 사용할 수 있게 담아주는 클래스
@ConfigurationProperties(prefix = "cloud.aws")
public record S3Properties(
        String region,
        S3 s3,
        Credentials credentials
) {
    public record S3(String bucket) {
    }

    public record Credentials(
            String accessKey,
            String secretKey
    ) {
    }
}