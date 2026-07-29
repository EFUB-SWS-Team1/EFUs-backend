package com.efus.backend.infra.ocr;

import com.efus.backend.infra.s3.S3Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;

@Configuration
public class TextractConfig {

    @Bean
    public TextractClient textractClient(S3Properties properties) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.credentials().accessKey(),
                properties.credentials().secretKey()
        );

        return TextractClient.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}