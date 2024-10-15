package com.test.microservices.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.RequestMapper;
import com.test.microservices.services.NotificationService;
import com.test.microservices.services.NotificationServiceImpl;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    @Autowired
    private NotificationServiceImpl servImpl;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveNotification(@RequestBody Notifications notification) throws JsonProcessingException {
        notificationService.saveNotification(notification);
        return new ResponseEntity<>("Notification saved", HttpStatus.CREATED);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Notifications>> getActiveNotifications() throws JsonProcessingException {
        List<Notifications> activeNotifications = notificationService.getActiveNotifications();
        return new ResponseEntity<>(activeNotifications, HttpStatus.OK);
    }
    @PutMapping("/updateOne")
    public ResponseEntity<String> updateSingleNotification(@RequestParam String NotificationId,int activeFlag) throws JsonProcessingException{
    	notificationService.updateSingleNotificationStatus(NotificationId, activeFlag);
    	return new ResponseEntity<>("Request Processed Successfully", HttpStatus.OK);
    }
    @PutMapping("/updateMany")
    public ResponseEntity<String> updateNotifications(@RequestBody List<RequestMapper> NotificationReq) throws JsonProcessingException{
    	notificationService.updateNotificationStatuses(NotificationReq);
    	return new ResponseEntity<>("Request Processed Successfully", HttpStatus.OK);
    }
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteNotifications(@RequestParam String NotificationId) throws JsonProcessingException{
    	notificationService.deleteNotification(NotificationId);
    	return new ResponseEntity<>("Request Processed Successfully", HttpStatus.OK);
    }
    
    
//    @GetMapping("/allactive")
//    public ResponseEntity<List<Notifications>> getAllActiveNotifications() throws JsonProcessingException, InterruptedException, ExecutionException {
//        CompletableFuture<List<Notifications>> activeNotifications = servImpl.getAllNotificationsAsync();
//        System.out.println(activeNotifications.get());
//        return new ResponseEntity<>(null, HttpStatus.OK);
//    }
}
