package com.test.microservices.services.impl;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Alerts;
import com.test.microservices.entity.Notifications;
public interface AlertStore {

	List<Alerts> getActiveNotifications() throws JsonProcessingException;
	void saveAlert(Alerts alert);

}
