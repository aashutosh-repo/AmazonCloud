package com.test.microservices.controller;

import java.util.List;

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
import com.test.microservices.error.NotificationException;
import com.test.microservices.mapper.RequestMapper;
import com.test.microservices.services.NotificationService;
import com.test.microservices.services.NotificationServiceImpl;
import com.test.microservices.services.impl.NotificationInterface;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    
    public NotificationController(NotificationService notificationService,
			NotificationInterface notificationInterface) {
		super();
		this.notificationService = notificationService;
	}

	@PostMapping("/save")
    public ResponseEntity<String> saveNotification(@RequestBody Notifications notification) {
    	try {
            notificationService.saveNotification(notification);
            return new ResponseEntity<>("Notification saved", HttpStatus.CREATED);
        } catch (NotificationException ex) {
            // Handle the exception and return appropriate response
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveNotifications() throws JsonProcessingException {
//        List<Notifications> activeNotifications = notificationInterface.getActiveNotifications();
//        return new ResponseEntity<>(activeNotifications, HttpStatus.OK);
    	try {
            List<?> activeNotifications = notificationService.getActiveNotifications();
            return new ResponseEntity<>(activeNotifications, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>("An error occurred while fetching notifications.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        
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
