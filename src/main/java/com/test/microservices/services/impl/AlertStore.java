package com.test.microservices.services.impl;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Notifications;
public interface AlertStore {

	List<Notifications> getActiveNotifications() throws JsonProcessingException;

}
