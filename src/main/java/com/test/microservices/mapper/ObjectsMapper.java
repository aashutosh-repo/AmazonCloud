package com.test.microservices.mapper;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.microservices.entity.Notifications;

@Component
public class ObjectsMapper<T>  {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(T object) throws JsonProcessingException {
        objectMapper.findAndRegisterModules();
        return objectMapper.writeValueAsString(object);
    }

    public T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        objectMapper.findAndRegisterModules();
        return objectMapper.readValue(json, clazz);
    }
}
