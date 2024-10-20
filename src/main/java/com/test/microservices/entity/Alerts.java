package com.test.microservices.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Alerts {
	private int alertId;
	private String title;
	private String messge;
	private LocalDateTime timestamp;
}
