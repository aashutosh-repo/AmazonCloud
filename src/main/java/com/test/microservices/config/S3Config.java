package com.test.microservices.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.credentials.access-key-id}")
    private String accessKeyId;

    @Value("${aws.s3.credentials.secret-access-key}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3Client.builder()
                       .region(Region.of(region))
                       .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                       .build();
    }
    
    @Bean
    public S3AsyncClient s3AsyncClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3AsyncClient.builder()
                .region(Region.of(region))  // Replace with your desired region
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
