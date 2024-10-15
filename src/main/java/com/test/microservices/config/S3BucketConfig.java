package com.test.microservices.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3BucketConfig {

    @Value("${aws.s3.bucketName1}")
    private String bucket1Name;

    @Value("${aws.s3.bucketName2}")
    private String bucket2Name;

    // Getters for bucket names
    public String getBucket1Name() {
        return bucket1Name;
    }

    public String getBucket2Name() {
        return bucket2Name;
    }
}
