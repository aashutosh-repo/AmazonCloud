package com.test.microservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.ObjectsMapper;
import com.test.microservices.mapper.RequestMapper;
import com.test.microservices.services.NotificationService;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

class NotificationServiceIntegrationTest {

    private S3Client s3Client;
    private ObjectsMapper<Notifications> notificationMapper;
    private NotificationService notificationService;
    private final String testBucket = "test-bucket";
    private Notifications notifications;

    @BeforeEach
    void setUp() {
        s3Client = Mockito.mock(S3Client.class);
        notificationMapper = Mockito.mock(ObjectsMapper.class);
        notificationService = new NotificationService(s3Client, notificationMapper, testBucket);
        notifications = new Notifications(
                "12345",
                "Test Notification",
                "This is a test message",
                LocalDate.now(),
                LocalDateTime.now(),
                1,
                true
        );
    }

    @Test
    void testSaveNotification() throws Exception {
        Notifications notification = new Notifications();
        notification.setNotificationId("12345");
        notification.setTitle("Test Notification");
        notification.setMessage("This is a test notification");
        notification.setIsActive(1);

        String expectedJson = "{\"notificationId\":\"12345\",\"title\":\"Test Notification\",\"message\":\"This is a test notification\",\"isActive\":1}";
        Mockito.when(notificationMapper.toJson(any(Notifications.class))).thenReturn(expectedJson);

        notificationService.saveNotification(notification);

        Mockito.verify(s3Client).putObject(
            Mockito.any(PutObjectRequest.class),
            Mockito.any(RequestBody.class)
        );
    }

    @Test
    void testGetActiveNotifications() throws Exception {
        // Mock S3 responses
        ListObjectsV2Response listResponse = ListObjectsV2Response.builder()
            .contents(S3Object.builder().key("notifications/12345.json").build())
            .isTruncated(false)
            .build();
        Mockito.when(s3Client.listObjectsV2(Mockito.any(ListObjectsV2Request.class))).thenReturn(listResponse);

        String notificationJson = "{\"notificationId\":\"12345\",\"title\":\"Test\",\"message\":\"Active Notification\",\"isActive\":1}";
        Mockito.when(s3Client.getObjectAsBytes(Mockito.any(GetObjectRequest.class)))
            .thenReturn(ResponseBytes.fromByteArray(null, notificationJson.getBytes()));
        Mockito.when(notificationMapper.fromJson(notificationJson, Notifications.class))
            .thenReturn(notifications);

        List<Notifications> notifications = notificationService.getActiveNotifications();

        assertEquals(1, notifications.size());
        assertEquals("12345", notifications.get(0).getNotificationId());
    }

    @Test
    void testUpdateSingleNotificationStatus() throws Exception {
        String key = "notifications/12345.json";
        String notificationJson = "{\"notificationId\":\"12345\",\"title\":\"Test\",\"message\":\"Old Message\",\"isActive\":0}";

        // Mock the S3 response for getting the object as bytes
        Mockito.when(s3Client.getObjectAsBytes(Mockito.any(GetObjectRequest.class)))
               .thenReturn(ResponseBytes.fromByteArray(null, notificationJson.getBytes()));

        // Create a Notifications object
        Notifications notification = new Notifications("12345", "Test", "Old Message", LocalDate.now(), LocalDateTime.now(), 0, false);
        
        // Mock the mapper behavior for converting JSON to object
        Mockito.when(notificationMapper.fromJson(notificationJson, Notifications.class)).thenReturn(notification);

        // Update the notification status
        notification.setIsActive(1);
        String updatedJson = "{\"notificationId\":\"12345\",\"title\":\"Test\",\"message\":\"Old Message\",\"isActive\":1}";

        // Mock the mapper behavior for converting the updated object back to JSON
        Mockito.when(notificationMapper.toJson(notification)).thenReturn(updatedJson);

        // Call the method under test
        notificationService.updateSingleNotificationStatus("12345", 1);

        // Verify that the S3 client's putObject method was called correctly
        Mockito.verify(s3Client).putObject(
            Mockito.any(PutObjectRequest.class),
            Mockito.any(RequestBody.class)
        );
    }

    @Test
    void testDeleteNotification() {
        String key = "notifications/12345.json";

        notificationService.deleteNotification("12345");

        Mockito.verify(s3Client).deleteObject(Mockito.any(DeleteObjectRequest.class));
    }

    @Test
    void testUpdateNotificationStatuses() throws Exception {
        List<RequestMapper> notificationRequests = new ArrayList<>();
        RequestMapper req1 = new RequestMapper("12345", 1);
        notificationRequests.add(req1);

        String notificationJson = "{\"notificationId\":\"12345\",\"title\":\"Test\",\"message\":\"Old Message\",\"isActive\":1}";

        Mockito.when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
            .thenReturn(ResponseBytes.fromByteArray(null, notificationJson.getBytes()));
        Notifications notification = notifications;
        Mockito.when(notificationMapper.fromJson(notificationJson, Notifications.class)).thenReturn(notification);

        notification.setIsActive(1);
        String updatedJson = "{\"notificationId\":\"12345\",\"title\":\"Test\",\"message\":\"Old Message\",\"isActive\":1}";
        Mockito.when(notificationMapper.toJson(notification)).thenReturn(updatedJson);

        notificationService.updateNotificationStatuses(notificationRequests);

        Mockito.verify(s3Client).putObject(
            Mockito.any(PutObjectRequest.class),
            Mockito.any(RequestBody.class)
        );
    }
}
