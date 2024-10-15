package com.test.microservices.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.test.microservices.mapper.NotificationMapper;
import com.test.microservices.services.NotificationService;

import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AppConfig {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Bean
    public NotificationMapper notificationMapper() {
        return new NotificationMapper();
    }

    @Bean
    public NotificationService notificationService(S3Client s3Client, NotificationMapper notificationMapper) {
        return new NotificationService(s3Client, notificationMapper, bucketName);
    }
}
