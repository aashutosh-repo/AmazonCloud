package com.test.microservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.test.microservices.config.S3BucketConfig;
import com.test.microservices.controller.NotificationController;
import com.test.microservices.entity.Notifications;
import com.test.microservices.error.NotificationException;
import com.test.microservices.mapper.ObjectsMapper;
import com.test.microservices.services.NotificationService;
import com.test.microservices.services.impl.NotificationInterface;

import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
class MicroservicesApplicationTests {

	@Test
	void contextLoads() {
	}
	
	
	@Mock
    private S3Client s3Client;

    @Mock
    private ObjectsMapper<Notifications> notificationMapper;

    @Mock
    private S3BucketConfig s3BucketConfig;
    @Mock
    private NotificationInterface notificationInterface;

    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private NotificationController notificationController;


    private Notifications notification;

    @BeforeEach
    void setUp() {
        notification = new Notifications();
        notification.setNotificationId("1234");
        notification.setIsActive(1);
    }

//    @Test
//    public void testSaveNotification() throws JsonProcessingException {
//        // Arrange
//        String bucketName = s3BucketConfig.getBucket1Name();
//        String key = "notifications/" + notification.getNotificationId() + ".json";
//        String notificationJson = "{\"notificationId\":\"123\"}";
//        when(notificationMapper.toJson(notification)).thenReturn(notificationJson);
//        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());
//
//        // Act
//        notificationService.saveNotification(notification);
//
//        // Assert
//        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
//        verify(notificationMapper, times(1)).toJson(notification);
//    }

    @Test
    public void testSaveNotification_NotificationException() {
        // Arrange: Mock the service to throw NotificationException
        doThrow(new NotificationException("Error converting notification to JSON"))
            .when(notificationService).saveNotification(any(Notifications.class));

        // Act: Call the controller method
        ResponseEntity<String> response = notificationController.saveNotification(notification);

        // Assert: Verify that the response contains the expected error status and message
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error converting notification to JSON", response.getBody());
    }
}
