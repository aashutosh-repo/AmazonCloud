package com.test.microservices.dto;

import java.util.List;

import com.test.microservices.entity.Notifications;

import lombok.Data;

@Data
public class AllNotifications {
	private List<Notifications> allnotifications;

}
