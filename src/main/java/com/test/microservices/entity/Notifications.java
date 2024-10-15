package com.test.microservices.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Notifications  implements Serializable{
	private String notificationId;
    private String title;
    private String message;
    private LocalDate startDate;
    private LocalDateTime notificationAddTime;
    private int isActive;
}
