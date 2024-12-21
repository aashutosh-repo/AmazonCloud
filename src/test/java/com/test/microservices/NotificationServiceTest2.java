package com.test.microservices;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Notifications;
import com.test.microservices.error.NotificationException;
import com.test.microservices.mapper.ObjectsMapper;
import com.test.microservices.services.NotificationService;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

class NotificationServiceTest2 {

    @Mock
    private S3Client s3Client;

    @Mock
    private ObjectsMapper<Notifications> notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

   private String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationService = new NotificationService(s3Client, notificationMapper, bucketName);
    }

    @Test
    void testSaveNotification_Success() throws JsonProcessingException {
        Notifications notification = new Notifications("1", "Test Title", "Test Message", LocalDate.now(), LocalDateTime.now(), 1, true);
        when(notificationMapper.toJson(notification)).thenReturn("{\"notificationId\":\"1\"}");

        notificationService.saveNotification(notification);

        verify(s3Client, times(1)).putObject(
            any(PutObjectRequest.class),
            any(RequestBody.class)
        );
    }

    @Test
    void testSaveNotification_JsonProcessingException() throws JsonProcessingException {
        Notifications notification = new Notifications("1", "Test Title", "Test Message", LocalDate.now(), LocalDateTime.now(), 1, true);
        when(notificationMapper.toJson(notification)).thenThrow(JsonProcessingException.class);

        assertThrows(NotificationException.class, () -> notificationService.saveNotification(notification));
    }

//    @Test
//    void testGetActiveNotifications_Success() throws JsonProcessingException {
//        String key = "notifications/1.json";
//        Notifications notification = new Notifications("1", "Test Title", "Test Message", LocalDate.now(), LocalDateTime.now(), 1, true);
//
//        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(
//            ListObjectsV2Response.builder().contents(S3Object.builder().key(key).build()).build()
//        );
//        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(
//            ResponseBytes.fromByteArray(GetObjectRequest.builder().build(), "{\"isActive\":1}".getBytes())
//        );
//        when(notificationMapper.fromJson(anyString(), eq(Notifications.class))).thenReturn(notification);
//
//        List<Notifications> activeNotifications = notificationService.getActiveNotifications();
//
//        assertEquals(1, activeNotifications.size());
//        assertEquals("1", activeNotifications.get(0).getNotificationId());
//    }

//    @Test
//    void testUpdateSingleNotificationStatus_Success() throws JsonProcessingException {
//        String notificationId = "1";
//        String key = "notifications/1.json";
//        Notifications notification = new Notifications(notificationId, "Title", "Message", LocalDate.now(), LocalDateTime.now(), 1, true);
//
//        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(
//            ResponseBytes.fromByteArray(GetObjectRequest.builder().build(), "{\"isActive\":1}".getBytes())
//        );
//        when(notificationMapper.fromJson(anyString(), eq(Notifications.class))).thenReturn(notification);
//        when(notificationMapper.toJson(any(Notifications.class))).thenReturn("{\"isActive\":0}");
//
//        notificationService.updateSingleNotificationStatus(notificationId, 0);
//
//        verify(s3Client, times(1)).putObject(
//            any(PutObjectRequest.class),
//            any(RequestBody.class)
//        );
//    }

    @Test
    void testDeleteNotification_Success() {
        String notificationId = "1";

        notificationService.deleteNotification(notificationId);

        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void testDeleteNotification_NoSuchKeyException() {
        String notificationId = "1";

        doThrow(NoSuchKeyException.builder().build()).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        assertDoesNotThrow(() -> notificationService.deleteNotification(notificationId));
    }

    @Test
    void testGenerateUniqueKey() {
        String key = NotificationService.generateUniqueKey();
        assertNotNull(key);
        assertTrue(key.matches("\\d+-\\d{4}"));
    }
}
