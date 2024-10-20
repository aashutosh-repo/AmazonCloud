package com.test.microservices.mapper;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.microservices.entity.Notifications;

@Component
public class ObjectsMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(Notifications notification) throws JsonProcessingException {
    	objectMapper.findAndRegisterModules();
        return objectMapper.writeValueAsString(notification);
    }

    public Notifications fromJson(String json) throws JsonProcessingException {
    	objectMapper.findAndRegisterModules();
        return objectMapper.readValue(json, Notifications.class);
    }
}
