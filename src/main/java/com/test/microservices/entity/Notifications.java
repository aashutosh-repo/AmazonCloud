package com.test.microservices.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class Notifications{
	private String notificationId;
    private String title;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    private LocalDateTime notificationAddTime;
    private int isActive;
    private boolean enabled;
}
