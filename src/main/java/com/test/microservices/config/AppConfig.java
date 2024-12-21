package com.test.microservices.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.test.microservices.entity.Alerts;
import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.ObjectsMapper;
import com.test.microservices.services.AlertService;
import com.test.microservices.services.NotificationService;
import com.test.microservices.services.impl.AlertStore;

import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AppConfig {

    @Value("${aws.s3.bucketName}")
    private String bucketName;
    
	@Autowired
	private S3BucketConfig s3BucketConfig;
	@Autowired
	private ObjectsMapper<Alerts> mapper;

    @Bean
    public ObjectsMapper<Notifications> notificationMapper() {
        return new ObjectsMapper<Notifications>();
    }
    
    @Qualifier(value = "s3Client")
    private S3Client s3Client;

    @Bean
    public NotificationService notificationService(S3Client s3Client, ObjectsMapper<Notifications> notificationMapper) {
        return new NotificationService(s3Client, notificationMapper, bucketName);
    }
    
	@Bean(name = "objectStore")
	public AlertStore alertStore(S3Client client) {
		return new AlertService(client,s3BucketConfig.getBucket1Name(),mapper);
	}
}

