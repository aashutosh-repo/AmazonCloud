package com.test.microservices.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Notifications;
import com.test.microservices.error.NotificationException;
import com.test.microservices.mapper.ObjectsMapper;
import com.test.microservices.mapper.RequestMapper;
import com.test.microservices.services.impl.NotificationInterface;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class NotificationService implements NotificationInterface {
    
    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    private final S3Client s3Client;
    private final ObjectsMapper<Notifications> notificationMapper;

    private String bucketName;
    
    @Autowired
    public NotificationService(S3Client s3Client, ObjectsMapper<Notifications> notificationMapper, String bucketName) {
        this.s3Client = s3Client;
        this.notificationMapper = notificationMapper;
        this.bucketName = bucketName;
    }

    
    public NotificationService(S3Client s3Client, ObjectsMapper notificationMapper) {
        this.s3Client = s3Client;
        this.notificationMapper = notificationMapper;
    }
    
    @Override
    public void saveNotification(Notifications notification) {
        try {
            notification.setNotificationId(generateUniqueKey());
            String key = "notifications/" + notification.getNotificationId() + ".json";
            String notificationJson = notificationMapper.toJson(notification);
            s3Client.putObject(PutObjectRequest.builder()
                                                .bucket(bucketName)
                                                .key(key).build(),
                                RequestBody.fromString(notificationJson));
           // System.out.println("Notification Successfully Saved: " + notification.getNotificationId());
        } catch (JsonProcessingException e) {
            //logger.error("Error converting notification to JSON: " + e.getMessage(), e);
            throw new NotificationException("Error converting notification to JSON");
        } 
//            catch (Exception e) {
//            logger.error("Error saving notification: " + e.getMessage(), e);
//        }
    }

    @Override
    public List<Notifications> getActiveNotifications() {
        List<Notifications> activeNotifications = new ArrayList<>();
        try {
            List<String> keys = listNotificationKeys();
            for (String key : keys) {
                String json = s3Client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucketName).key(key).build())
                        .asUtf8String();
                Notifications notification = notificationMapper.fromJson(json, Notifications.class);
                if (notification.getIsActive() == 1) {
                    activeNotifications.add(notification);
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("Error processing notifications: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error retrieving active notifications: " + e.getMessage(), e);
        }
        return activeNotifications;
    }

    private List<String> listNotificationKeys() {
        List<String> keys = new ArrayList<>();
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response;

        try {
            do {
                response = s3Client.listObjectsV2(listObjectsV2Request);

                for (S3Object object : response.contents()) {
                    keys.add(object.key());
                }

                listObjectsV2Request = listObjectsV2Request.toBuilder()
                        .continuationToken(response.nextContinuationToken())
                        .build();
            } while (response.isTruncated());
        } catch (Exception e) {
            logger.error("Error listing notification keys: " + e.getMessage(), e);
        }

        return keys;
    }
    
    @Override
    public void updateSingleNotificationStatus(String notificationId, int isActive) {
        String key = "notifications/" + notificationId + ".json";

        try {
            String json = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                                                                     .bucket(bucketName)
                                                                     .key(key)
                                                                     .build())
                                  .asUtf8String();
            Notifications notification = notificationMapper.fromJson(json, Notifications.class);
            notification.setIsActive(isActive);

            String updatedNotificationJson = notificationMapper.toJson(notification);
            s3Client.putObject(PutObjectRequest.builder()
                                                .bucket(bucketName)
                                                .key(key)
                                                .build(),
                               RequestBody.fromString(updatedNotificationJson));
        } catch (JsonProcessingException e) {
            logger.error("Error processing notification: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error updating notification status: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void updateNotificationStatuses(List<RequestMapper> notificationIds) {
        notificationIds.forEach(notificationId -> {
            try {
                String key = "notifications/" + notificationId.getNotificationId() + ".json";

                String json = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                                                                         .bucket(bucketName)
                                                                         .key(key)
                                                                         .build())
                                      .asUtf8String();
                Notifications notification = notificationMapper.fromJson(json, Notifications.class);
                notification.setIsActive(notificationId.getActivate());

                String updatedNotificationJson = notificationMapper.toJson(notification);
                s3Client.putObject(PutObjectRequest.builder()
                                                    .bucket(bucketName)
                                                    .key(key)
                                                    .build(),
                                   RequestBody.fromString(updatedNotificationJson));
            } catch (JsonProcessingException e) {
                logger.error("Error processing notification ID: " + notificationId.getNotificationId() + " - " + e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Error updating notification ID: " + notificationId.getNotificationId() + " - " + e.getMessage(), e);
            }
        });
    }
    
    
    @Override
    public void deleteNotification(String notificationId) {
        String key = "notifications/" + notificationId + ".json";

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.debug("Notification Successfully Deleted: " + notificationId);
        } catch (NoSuchKeyException e) {
            logger.debug("Notification with ID: " + notificationId + " does not exist.");
        } catch (Exception e) {
            logger.error("Error occurred while deleting notification: " + notificationId, e);
        }
    }

    @Override
    public void deleteNotifications(List<String> notificationIds) {
        for (String notificationId : notificationIds) {
            String key = "notifications/" + notificationId + ".json";

            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                s3Client.deleteObject(deleteObjectRequest);
                logger.debug("Notification Successfully Deleted: " + notificationId);
            } catch (NoSuchKeyException e) {
                logger.debug("Notification with ID: " + notificationId + " does not exist.");
            } catch (Exception e) {
                logger.error("Error occurred while deleting notification: " + notificationId, e);
            }
        }
    }
    
    public static String generateUniqueKey() {
        Random RANDOM = new Random();
        long timestamp = Instant.now().toEpochMilli();
        int randomPart = RANDOM.nextInt(10000); // Adjust the range as needed
        return String.format("%d-%04d", timestamp, randomPart);
    }
}
