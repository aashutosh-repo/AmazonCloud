package com.test.microservices.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Notifications{
	private String notificationId;
    private String title;
    private String message;
    private LocalDate startDate;
    private LocalDateTime notificationAddTime;
    private int isActive;
}
