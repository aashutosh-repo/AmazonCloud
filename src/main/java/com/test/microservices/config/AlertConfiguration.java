package com.test.microservices.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.test.microservices.services.AlertService;
import com.test.microservices.services.impl.AlertStore;

import software.amazon.awssdk.services.s3.S3Client;

//@Configuration
public class AlertConfiguration {
//	@Autowired
//	private S3BucketConfig s3BucketConfig;
//	private S3Client client;
//	@Bean
//	public AlertStore alertStore(@Qualifier("s3ClientAlert") S3Client client) {
//		return new AlertService(client,s3BucketConfig.getBucket1Name());
//	}
}