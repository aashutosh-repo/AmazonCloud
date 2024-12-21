package com.test.microservices.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Alerts;
import com.test.microservices.error.NotificationException;
import com.test.microservices.mapper.ObjectsMapper;
import com.test.microservices.services.impl.AlertStore;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class AlertService implements AlertStore{
	
	//@Value("${aws.s3.bucketName}")
	private String bucket;
	private S3Client client;
	ObjectsMapper<Alerts> mapper;
	
    public AlertService(S3Client client,String bucket,  ObjectsMapper<Alerts> mapper) {
		super();
		this.bucket = bucket;
		this.client = client;
		this.mapper = mapper;
	}
    
	@Override
    public void saveAlert(Alerts alert) {
        try {
        	alert.setAlertId(generateUniqueKey());
            String key = "Alert/" + alert.getAlertId() + ".json";
            String alertJson = mapper.toJson(alert);
            client.putObject(PutObjectRequest.builder()
                                                .bucket(bucket)
                                                .key(key).build(),
                                RequestBody.fromString(alertJson));
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
	public List<Alerts> getActiveNotifications() throws JsonProcessingException {
        List<Alerts> activeNotifications = new ArrayList<>();
        ObjectsMapper<Alerts> mapper = new ObjectsMapper<Alerts>();
            List<String> keys = listNotificationKeys();
            for (String key : keys) {
                String json = client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build())
                        .asUtf8String();
                Alerts notification = mapper.fromJson(json, Alerts.class);
                if (notification.getIsActive()==1) {
                    activeNotifications.add(notification);
                }
            }
        return activeNotifications;
    }

    private List<String> listNotificationKeys() {
    	List<String> keys = new ArrayList<>();
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix("Alert/")
                .build();

        ListObjectsV2Response response;

        do {
            response = client.listObjectsV2(listObjectsV2Request);

            for (S3Object object : response.contents()) {
                keys.add(object.key());
            }

            listObjectsV2Request = listObjectsV2Request.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();
        } while (response.isTruncated());

        return keys;
    }
    
    public static String generateUniqueKey() {
        Random RANDOM = new Random();
        long timestamp = Instant.now().toEpochMilli();
        int randomPart = RANDOM.nextInt(10000); // Adjust the range as needed
        return String.format("%d-%04d", timestamp, randomPart);
    }

}
