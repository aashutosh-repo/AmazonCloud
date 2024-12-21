package com.test.microservices.services.impl;

import java.util.List;

import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.RequestMapper;


public interface NotificationInterface {
	void saveNotification(Notifications notification);
	List<Notifications> getActiveNotifications();
	//List<String> listNotificationKeys();
	void updateSingleNotificationStatus(String notificationId, int isActive);
	void updateNotificationStatuses(List<RequestMapper> notificationIds);
	void deleteNotification(String notificationId);
	void deleteNotifications(List<String> notificationIds);

}
