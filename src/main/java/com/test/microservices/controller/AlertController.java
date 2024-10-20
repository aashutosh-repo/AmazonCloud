package com.test.microservices.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Notifications;
import com.test.microservices.services.impl.AlertStore;

@RestController
@RequestMapping("/alert")
public class AlertController {
	
    private final AlertStore alertobjectStore;
    @Autowired
    public AlertController(AlertStore alertobjectStore) {
        this.alertobjectStore = alertobjectStore;
    }

    @GetMapping("/active")
    public ResponseEntity<List<Notifications>> getActiveNotifications() throws JsonProcessingException {
        List<Notifications> activeNotifications = alertobjectStore.getActiveNotifications();
        return new ResponseEntity<>(activeNotifications, HttpStatus.OK);
    }
}
