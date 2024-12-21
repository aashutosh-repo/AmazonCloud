package com.test.microservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.ObjectsMapper;
import com.test.microservices.mapper.RequestMapper;
import com.test.microservices.services.NotificationService;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private ObjectsMapper<Notifications> notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private Notifications notification;
    
    private List<Notifications> notificationsList;

    @BeforeEach
    void setUp() {
       // MockitoAnnotations.openMocks(this);
        notification = new Notifications();
        notification.setNotificationId("1234");
        notification.setIsActive(1);
        notificationsList = new ArrayList<>();
        notificationsList.add(notification);
    }

    @Test
    void testSaveNotification() throws JsonProcessingException {
        when(notificationMapper.toJson(any(Notifications.class))).thenReturn("{ \"notificationId\": \"1234\", \"isActive\": 1 }");

        notificationService.saveNotification(notification);

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testGetActiveNotifications() throws JsonProcessingException {
        // Mocking the S3 response for listObjectsV2
        List<S3Object> s3Objects = new ArrayList<>();
        s3Objects.add(S3Object.builder().key("notifications/1234.json").build());

        ListObjectsV2Response listObjectsV2Response = ListObjectsV2Response.builder()
                .contents(s3Objects)
                .isTruncated(false)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Response);

        // Mocking the GetObjectResponse
        String notificationJson = "{ \"notificationId\": \"1234\", \"isActive\": 1 }";
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(mock(GetObjectResponse.class), notificationJson.getBytes());

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);
        when(notificationMapper.fromJson(anyString(), eq(Notifications.class))).thenReturn(notification);

        List<Notifications> activeNotifications = notificationService.getActiveNotifications();

        assertEquals(1, activeNotifications.size());
        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
    }

    @Test
    void testUpdateSingleNotificationStatus() throws JsonProcessingException {
        String notificationJson = "{ \"notificationId\": \"1234\", \"isActive\": 0 }";
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(mock(GetObjectResponse.class), notificationJson.getBytes());

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);
        when(notificationMapper.fromJson(anyString(),eq(Notifications.class))).thenReturn(notification);
        when(notificationMapper.toJson(any(Notifications.class))).thenReturn("{ \"notificationId\": \"1234\", \"isActive\": 1 }");

        notificationService.updateSingleNotificationStatus("1234", 1);

        verify(s3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUpdateNotificationStatuses() throws JsonProcessingException {
        RequestMapper requestMapper = new RequestMapper();
        requestMapper.setNotificationId("1234");
        requestMapper.setActivate(1);
        List<RequestMapper> requestMappers = new ArrayList<>();
        requestMappers.add(requestMapper);

        String notificationJson = "{ \"notificationId\": \"1234\", \"isActive\": 0 }";
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(mock(GetObjectResponse.class), notificationJson.getBytes());

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);
        when(notificationMapper.fromJson(anyString(),eq(Notifications.class))).thenReturn(notification);
        when(notificationMapper.toJson(any(Notifications.class))).thenReturn("{ \"notificationId\": \"1234\", \"isActive\": 1 }");

        notificationService.updateNotificationStatuses(requestMappers);

        verify(s3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testDeleteNotification() {
        notificationService.deleteNotification("1234");

        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void testDeleteNotifications() {
        List<String> notificationIds = new ArrayList<>();
        notificationIds.add("1234");

        notificationService.deleteNotifications(notificationIds);

        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }
}
