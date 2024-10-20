package com.test.microservices.config;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3AsyncClient;
//import software.amazon.awssdk.services.s3.S3Client;
//
//@Configuration
//public class S3Config {
//
//    @Value("${aws.region}")
//    private String region;
//
//    @Value("${aws.s3.credentials.access-key-id}")
//    private String accessKeyId;
//
//    @Value("${aws.s3.credentials.secret-access-key}")
//    private String secretAccessKey;
//
//    @Bean(name= "s3Client")
//    public S3Client s3Client() {
//        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
//        return S3Client.builder()
//                       .region(Region.of(region))
//                       .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
//                       .build();
//    }
//    
//    @Bean(name= "s3ClientAlert")
//    public S3Client s3ClientTest() {
//        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
//        return S3Client.builder()
//                       .region(Region.of(region))
//                       .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
//                       .build();
//    }
//    @Bean(name= "s3AsyncClient")
//    public S3AsyncClient s3AsyncClient() {
//        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
//        return S3AsyncClient.builder()
//                .region(Region.of(region))  // Replace with your desired region
//                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
//                .build();
//    }
//}


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config{
	
    @Configuration
	@Profile("dev")
	static class S3DevConfig  {

		@Value("${aws.region}")
		private String region;

		@Value("${aws.s3.credentials.access-key-id}")
		private String accessKeyId;

		@Value("${aws.s3.credentials.secret-access-key}")
		private String secretAccessKey;

		@Bean(name = "s3Client")
		public S3Client s3Client() {
			AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
			return S3Client.builder()
                       .region(Region.of(region))
                       .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                       .build();
		}

		@Bean(name = "s3AsyncClient")
		public S3AsyncClient s3AsyncClient() {
			AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
			return S3AsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
		}
	}

    @Configuration
	@Profile("prod")
	static class S3ProdConfig {

		@Value("${aws.region.prod}")
		private String region;

		@Value("${aws.s3.credentials.access-key-id.prod}")
		private String accessKeyId;

		@Value("${aws.s3.credentials.secret-access-key.prod}")
		private String secretAccessKey;

		@Bean(name = "s3Client")
		public S3Client s3Client() {
			AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
			return S3Client.builder()
                       .region(Region.of(region))
                       .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                       .build();
		}
		

		@Bean(name = "s3AsyncClient")
		public S3AsyncClient s3AsyncClient() {
			AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
			return S3AsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
		}
	}
}

