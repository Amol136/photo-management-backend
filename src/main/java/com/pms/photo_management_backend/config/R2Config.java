package com.pms.photo_management_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class R2Config {

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;

    @Value("${cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;

    // ==========================================
    // CREDENTIALS
    // ==========================================

    private StaticCredentialsProvider credentialsProvider() {

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(
                        accessKey,
                        secretKey
                );

        return StaticCredentialsProvider.create(
                credentials
        );
    }

    // ==========================================
    // S3 CLIENT
    // Upload / Delete
    // ==========================================

    @Bean
    public S3Client r2S3Client() {

        return S3Client.builder()
                .endpointOverride(
                        URI.create(endpoint)
                )
                .region(
                        Region.of("auto")
                )
                .credentialsProvider(
                        credentialsProvider()
                )
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                )
                .build();
    }

    // ==========================================
    // S3 PRESIGNER
    // Private Photo Signed URL
    // ==========================================

    @Bean
    public S3Presigner r2S3Presigner() {

        return S3Presigner.builder()
                .endpointOverride(
                        URI.create(endpoint)
                )
                .region(
                        Region.of("auto")
                )
                .credentialsProvider(
                        credentialsProvider()
                )
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                )
                .build();
    }
}