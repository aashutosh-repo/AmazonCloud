package com.test.microservices.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Alerts;
import com.test.microservices.entity.Notifications;
import com.test.microservices.error.NotificationException;
import com.test.microservices.services.impl.AlertStore;

@RestController
@RequestMapping("/alert")
public class AlertController {
	
    private final AlertStore objectStore;
    @Autowired
    public AlertController(AlertStore objectStore) {
        this.objectStore = objectStore;
    }
    
    @PostMapping("/save")
    public ResponseEntity<String> saveNotification(@RequestBody Alerts alert) {
    	try {
    		objectStore.saveAlert(alert);
            return new ResponseEntity<>("Alert saved", HttpStatus.CREATED);
        } catch (NotificationException ex) {
            // Handle the exception and return appropriate response
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<Alerts>> getActiveNotifications() throws JsonProcessingException {
        List<Alerts> activeNotifications = objectStore.getActiveNotifications();
        return new ResponseEntity<>(activeNotifications, HttpStatus.OK);
    }
}
