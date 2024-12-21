package com.test.microservices;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.controller.NotificationController;
import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.RequestMapper;
import com.test.microservices.services.NotificationService;
import com.test.microservices.services.impl.NotificationInterface;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerUnitTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationInterface notificationInterface;

    @InjectMocks
    private NotificationController notificationController;

    private Notifications notification;
    private List<Notifications> activeNotifications;
    private RequestMapper requestMapper;

    @BeforeEach
    public void setup() {
        notification = new Notifications();
        notification.setNotificationId("1");
        notification.setTitle("Test Notification");
        notification.setMessage("This is a test notification");
        notification.setIsActive(1);
        notification.setEnabled(true);
        activeNotifications = List.of(notification);
        requestMapper = new RequestMapper();
        requestMapper.setNotificationId("1");
        requestMapper.setActivate(1);
    }

    @Test
    public void testSaveNotification() throws JsonProcessingException {
        // Arrange
//        doNothing().when(notificationInterface).saveNotification(any(Notifications.class));

        // Act
        ResponseEntity<String> response = notificationController.saveNotification(notification);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Notification saved", response.getBody());
        verify(notificationService, times(1)).saveNotification(any(Notifications.class));
    }

    @Test
    public void testGetActiveNotifications() throws JsonProcessingException {
        // Arrange
        when(notificationService.getActiveNotifications()).thenReturn(activeNotifications);

        // Act
        ResponseEntity<?> response = notificationController.getActiveNotifications();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(activeNotifications, response.getBody());
        verify(notificationService, times(1)).getActiveNotifications();
    }

    @Test
    public void testUpdateSingleNotification() throws JsonProcessingException {
        // Arrange
        doNothing().when(notificationService).updateSingleNotificationStatus(anyString(), anyInt());

        // Act
        ResponseEntity<String> response = notificationController.updateSingleNotification("NotificationId", 1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Request Processed Successfully", response.getBody());
        verify(notificationService, times(1)).updateSingleNotificationStatus(anyString(), anyInt());
    }

    @Test
    public void testUpdateNotifications() throws JsonProcessingException {
        // Arrange
        doNothing().when(notificationService).updateNotificationStatuses(anyList());

        // Act
        ResponseEntity<String> response = notificationController.updateNotifications(List.of(requestMapper));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Request Processed Successfully", response.getBody());
        verify(notificationService, times(1)).updateNotificationStatuses(anyList());
    }

    @Test
    public void testDeleteNotifications() throws JsonProcessingException {
        // Arrange
        doNothing().when(notificationService).deleteNotification(anyString());

        // Act
        ResponseEntity<String> response = notificationController.deleteNotifications("NotificationId");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Request Processed Successfully", response.getBody());
        verify(notificationService, times(1)).deleteNotification(anyString());
    }
}