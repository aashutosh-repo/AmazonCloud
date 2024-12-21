package com.test.microservices.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alerts {
	private String alertId;
	private String title;
	private String message;
	private LocalDateTime timestamp;
	private int isActive;
}
