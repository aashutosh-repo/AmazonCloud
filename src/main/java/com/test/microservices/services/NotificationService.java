package com.test.microservices.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.ObjectsMapper;
import com.test.microservices.mapper.RequestMapper;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

//@Service
public class NotificationService{
	
	private static final Logger logger = LogManager.getLogger(NotificationService.class);

    private final S3Client s3Client;
    private final ObjectsMapper<Notifications> notificationMapper;

    private String bucketName;
	
    public NotificationService(S3Client s3Client, ObjectsMapper notificationMapper, String bucketName) {
		super();
		this.s3Client = s3Client;
		this.notificationMapper = notificationMapper;
		this.bucketName = bucketName;
	}

    public NotificationService(S3Client s3Client, ObjectsMapper notificationMapper) {
        this.s3Client = s3Client;
        this.notificationMapper = notificationMapper;
    }
    
    public void saveNotification(Notifications notification) throws JsonProcessingException {
    	//String bucketName = s3BucketConfig.getBucket1Name();  //Uncomment when Multiple Bucket
    	notification.setNotificationId(generateUniqueKey());
            String key = "notifications/" + notification.getNotificationId() + ".json";
            String notificationJson = notificationMapper.toJson(notification);
            s3Client.putObject(PutObjectRequest.builder()
            								.bucket(bucketName)
            								.key(key).build(),
                                RequestBody.fromString(notificationJson));
           System.out.println("Notification Successfully Saved : " + notification.getNotificationId());
    }

    
    public List<Notifications> getActiveNotifications() throws JsonProcessingException {
        List<Notifications> activeNotifications = new ArrayList<>();
            List<String> keys = listNotificationKeys();
            for (String key : keys) {
                String json = s3Client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucketName).key(key).build())
                        .asUtf8String();
                Notifications notification = notificationMapper.fromJson(json, Notifications.class);
                if (notification.getIsActive()==1) {
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
    
    public void updateSingleNotificationStatus(String notificationId, int isActive) throws JsonProcessingException {
        String key = "notifications/" + notificationId + ".json";

        // Retrieve the notification
        String json = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                                                                 .bucket(bucketName)
                                                                 .key(key)
                                                                 .build())
                              .asUtf8String();
        Notifications notification = notificationMapper.fromJson(json, Notifications.class);

        // Update the notification status
        notification.setIsActive(isActive);

        // Save the updated notification back to S3
        String updatedNotificationJson = notificationMapper.toJson(notification);
        s3Client.putObject(PutObjectRequest.builder()
                                            .bucket(bucketName)
                                            .key(key)
                                            .build(),
                           RequestBody.fromString(updatedNotificationJson));
    }
    
    public void updateNotificationStatuses(List<RequestMapper> notificationIds) throws JsonProcessingException {
        notificationIds.stream().forEach(notificationId -> {
            try {
                String key = "notifications/" + notificationId.getNotificationId() + ".json";

                // Retrieve the notification
                String json = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                                                                         .bucket(bucketName)
                                                                         .key(key)
                                                                         .build())
                                      .asUtf8String();
                Notifications notification = notificationMapper.fromJson(json, Notifications.class);

                // Update the notification status
                notification.setIsActive(notificationId.getActivate());

                // Save the updated notification back to S3
                String updatedNotificationJson = notificationMapper.toJson(notification);
                s3Client.putObject(PutObjectRequest.builder()
                                                    .bucket(bucketName)
                                                    .key(key)
                                                    .build(),
                                   RequestBody.fromString(updatedNotificationJson));
            } catch (JsonProcessingException e) {
                // Handle exception for individual notification
                e.printStackTrace();
            }
        });
    }
    
    public void deleteNotification(String notificationId) {
        // Construct the S3 key for the notification
        String key = "notifications/" + notificationId + ".json";

        try {
            // Create the DeleteObjectRequest
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)   // You can use s3BucketConfig.getBucket1Name() for multiple buckets
                    .key(key)
                    .build();

            // Execute the delete operation on the S3 bucket
            s3Client.deleteObject(deleteObjectRequest);
            logger.debug("Notification Successfully Deleted : " + notificationId);
        } catch (NoSuchKeyException e) {
        	logger.debug("Notification with ID: " + notificationId + " does not exist.");
        } catch (Exception e) {
            // Handle other exceptions that may occur
            e.printStackTrace();
        }
    }

    public void deleteNotifications(List<String> notificationIds) {
        for (String notificationId : notificationIds) {
            // Construct the S3 key for the notification
            String key = "notifications/" + notificationId + ".json";

            try {
                // Create the DeleteObjectRequest
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)   // You can use s3BucketConfig.getBucket1Name() for multiple buckets
                        .key(key)
                        .build();

                // Execute the delete operation on the S3 bucket
                s3Client.deleteObject(deleteObjectRequest);
                logger.debug("Notification Successfully Deleted : " + notificationId);
            } catch (NoSuchKeyException e) {
                logger.debug("Notification with ID: " + notificationId + " does not exist.");
            } catch (Exception e) {
                // Handle other exceptions that may occur
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
