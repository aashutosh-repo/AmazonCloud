package com.test.microservices.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Alerts;
import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.ObjectsMapper;
import com.test.microservices.services.impl.AlertStore;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class AlertService implements AlertStore{
	
	//@Value("${aws.s3.bucketName}")
	private String bucket;
	private S3Client client;
	
	public AlertService(S3Client client,String bucket) {
		super();
		this.client = client;
		this.bucket = bucket;
	}

	@Override
	public List<Notifications> getActiveNotifications() throws JsonProcessingException {
        List<Notifications> activeNotifications = new ArrayList<>();
        ObjectsMapper mapper = new ObjectsMapper();
            List<String> keys = listNotificationKeys();
            for (String key : keys) {
                String json = client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build())
                        .asUtf8String();
                Notifications notification = mapper.fromJson(json);
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

}
