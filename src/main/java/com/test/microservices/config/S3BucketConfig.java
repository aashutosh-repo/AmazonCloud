package com.test.microservices.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class S3BucketConfig {

    @Value("${aws.s3.bucketName}")
    private String bucket1Name;

    @Value("${aws.s3.bucketName2}")
    private String bucket2Name;
}
