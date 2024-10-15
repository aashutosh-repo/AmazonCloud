package com.test.microservices;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.config.S3BucketConfig;
import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.NotificationMapper;
import com.test.microservices.services.NotificationService;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@SpringBootTest
class MicroservicesApplicationTests {

//	@Test
//	void contextLoads() {
//	}
	
	
	@Mock
    private S3Client s3Client;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private S3BucketConfig s3BucketConfig;

    @InjectMocks
    private NotificationService notificationService;

    private Notifications notification;

    @BeforeEach
    public void setup() {
        notification = new Notifications();
    }

    @Test
    public void testSaveNotification() throws JsonProcessingException {
        // Arrange
        String bucketName = s3BucketConfig.getBucket1Name();
        String key = "notifications/" + notification.getNotificationId() + ".json";
        String notificationJson = "{\"notificationId\":\"123\"}";
        when(notificationMapper.toJson(notification)).thenReturn(notificationJson);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());

        // Act
        notificationService.saveNotification(notification);

        // Assert
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(notificationMapper, times(1)).toJson(notification);
    }

    @Test
    public void testSaveNotification_JsonProcessingException() throws JsonProcessingException {
        // Arrange
        when(notificationMapper.toJson(notification)).thenThrow(JsonProcessingException.class);

        // Act and Assert
        assertThrows(JsonProcessingException.class, () -> notificationService.saveNotification(notification));
    }


    

}
