package com.test.microservices.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.dto.AllNotifications;
import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.NotificationMapper;

import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class NotificationServiceImpl {
    private final S3AsyncClient s3AsyncClient;
    private final NotificationMapper notificationMapper;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    public NotificationServiceImpl(S3AsyncClient s3AsyncClient, NotificationMapper notificationMapper) {
        this.s3AsyncClient = s3AsyncClient;
        this.notificationMapper = notificationMapper;
    }

    public void saveNotificationAsync(Notifications notification) throws JsonProcessingException {
        notification.setNotificationId(generateUniqueKey());
        String key = "notifications/" + notification.getNotificationId() + ".json";
        String notificationJson = notificationMapper.toJson(notification);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromString(notificationJson))
                .thenApply(response -> null); // or handle the response
    }

public CompletableFuture<List<Notifications>> getAllNotificationsAsync() {
        // List objects with the specified prefix
	
	String prefix = "notifications/";
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        return s3AsyncClient.listObjectsV2(listObjectsRequest)
                .thenCompose(listObjectsResponse -> {
                    // Extract the keys from the response
                    List<String> keys = listObjectsResponse.contents().stream()
                            .map(s3Object -> s3Object.key())
                            .collect(Collectors.toList());

                    // Create a list of CompletableFutures to fetch each object
                    List<CompletableFuture<Notifications>> futures = new ArrayList<>();
                    for (String key : keys) {
                        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build();

                        CompletableFuture<Notifications> future = s3AsyncClient.getObject(getObjectRequest, AsyncResponseTransformer.toBytes())
                                .thenApply(response -> {
                                    String json = response.asUtf8String();
                                    try {
                                        // Deserialize JSON to Notifications object
                                        return notificationMapper.fromJson(json);
                                    } catch (JsonProcessingException e) {
                                        // Handle JSON parsing exception
                                        throw new RuntimeException("Failed to parse JSON", e);
                                    }
                                });

                        futures.add(future);
                    }

                    // Combine all futures into a single CompletableFuture that completes when all individual futures complete
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> {
                                List<Notifications> allNotifications = new ArrayList<>();
                                for (CompletableFuture<Notifications> future : futures) {
                                    Notifications notifications = future.join(); // Get the result
                                    if (notifications != null) {
                                        allNotifications.add(notifications);
                                    }
                                }
                                return allNotifications;
                            });
                });
    }
    public CompletableFuture<List<String>> listNotificationKeysAsync() {
        List<String> keys = new ArrayList<>();
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        return listObjectsV2RequestPaginator(listObjectsV2Request, keys)
                .thenApply(pagedKeys -> keys);
    }

    private CompletableFuture<List<String>> listObjectsV2RequestPaginator(ListObjectsV2Request request, List<String> accumulatedKeys) {
        return s3AsyncClient.listObjectsV2(request)
                .thenCompose(response -> {
                    accumulatedKeys.addAll(response.contents().stream().map(S3Object::key).collect(Collectors.toList()));
                    if (response.isTruncated()) {
                        ListObjectsV2Request nextRequest = request.toBuilder()
                                .continuationToken(response.nextContinuationToken())
                                .build();
                        return listObjectsV2RequestPaginator(nextRequest, accumulatedKeys);
                    } else {
                        return CompletableFuture.completedFuture(accumulatedKeys);
                    }
                });
    }

    public static String generateUniqueKey() {
        Random RANDOM = new Random();
        long timestamp = Instant.now().toEpochMilli();
        int randomPart = RANDOM.nextInt(10000); // Adjust the range as needed
        return String.format("%d-%04d", timestamp, randomPart);
    }
}
