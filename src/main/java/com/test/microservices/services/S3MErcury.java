package com.test.microservices.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.config.S3BucketConfig;
import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.ObjectsMapper;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class S3MErcury {
    private final S3Client s3Client;
    private final ObjectsMapper<Notifications> notificationMapper;
    
    @Autowired
    private S3BucketConfig s3BucketConfig;

//    @Value("${mercury.s3.bucket-name}")
    private String bucketName;

    public S3MErcury(S3Client s3Client, ObjectsMapper notificationMapper) {
        this.s3Client = s3Client;
        this.notificationMapper = notificationMapper;
    }

    public void saveNotification(Notifications notification) throws JsonProcessingException {
        notification.setNotificationId(generateUniqueKey());
        String key = "notifications/" + notification.getNotificationId() + ".json";
        String notificationJson = notificationMapper.toJson(notification);
        
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key).build(),
                RequestBody.fromString(notificationJson));
        
    }

    public List<Notifications> getActiveNotifications() throws JsonProcessingException {
        List<Notifications> activeNotifications = new ArrayList<>();
        List<String> keys = listNotificationKeys();
        
        for (String key : keys) {
            String json = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build()).asUtf8String();
            
            Notifications notification = notificationMapper.fromJson(json, Notifications.class);
            if (notification.getIsActive() == 1) {
                activeNotifications.add(notification);
            }
        }
        
        return activeNotifications;
    }

    private List<String> listNotificationKeys() {
        List<String> keys = new ArrayList<>();
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response;
        
        do {
            response = s3Client.listObjectsV2(listObjectsV2Request);

            for (S3Object object : response.contents()) {
                keys.add(object.key());
            }

            listObjectsV2Request = listObjectsV2Request.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();
        } while (response.isTruncated());

        return keys;
    }

    public void deleteNotification(String notificationId) {
        String key = "notifications/" + notificationId + ".json";

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (NoSuchKeyException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateUniqueKey() {
        Random RANDOM = new Random();
        long timestamp = Instant.now().toEpochMilli();
        int randomPart = RANDOM.nextInt(10000);
        return String.format("%d-%04d", timestamp, randomPart);
    }
}
