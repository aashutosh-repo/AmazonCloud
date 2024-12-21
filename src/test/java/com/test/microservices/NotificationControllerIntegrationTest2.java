package com.test.microservices;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.test.microservices.controller.NotificationController;
import com.test.microservices.entity.Notifications;
import com.test.microservices.mapper.RequestMapper;
import com.test.microservices.services.NotificationService;
import com.test.microservices.services.impl.NotificationInterface;


@ExtendWith(MockitoExtension.class)
public class NotificationControllerIntegrationTest2 {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    
    @InjectMocks
    private NotificationController controller;
    @Mock
    private NotificationInterface notificationInterface;
    @Mock
    private NotificationService notificationService;
    
    List<Notifications> sampleNotifications;
    
    @BeforeEach
    void setup() {
    	objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Register module for Java 8 date/time types
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        sampleNotifications = Arrays.asList(
                new Notifications("1", "Title1", "Message1",LocalDate.now(),LocalDateTime.now(), 1,true),
                new Notifications("2", "Title2", "Message2",LocalDate.now(),LocalDateTime.now(), 2,true)
            );
    }
    
    
    @Test
    public void testGetActiveNotifications_Success1() throws Exception {
        // Mocking the service response
        when(notificationInterface.getActiveNotifications()).thenReturn(sampleNotifications);

        // Performing the GET request and verifying response
        mockMvc.perform(get("/notifications/active"))
        .andDo(print())
            .andExpect(status().isOk())
            //.andExpect(content().json(objectMapper.writeValueAsString(sampleNotifications)))
            .andExpect(jsonPath("$[0].notificationId").value("1"))
            .andExpect(jsonPath("$[0].title").value("Title1"));
        System.out.println(sampleNotifications);
    }

    @Test
    public void testGetActiveNotifications_EmptyList() throws Exception {
        // Mocking the service response for empty list
        //when(notificationInterface.getActiveNotifications()).thenReturn(Collections.emptyList());

        // Performing the GET request and verifying response
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/active"))
        .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(Collections.emptyList())));
    }

    @Test
    public void testGetActiveNotifications_ExceptionHandling() throws Exception {
        // Mocking the service to throw an exception
        when(notificationInterface.getActiveNotifications()).thenThrow(new RuntimeException("Service error"));

        // Performing the GET request
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications/active"))
        .andDo(print())
            .andExpect(status().isInternalServerError())
           .andExpect(content().string("An error occurred while fetching notifications."));
    }
    
    
    @Test
    public void testSaveNotification_Success() throws Exception {
        Notifications notification = new Notifications(
                "1",
                "Test Notification",
                "This is a test message",
                LocalDate.now(),
                LocalDateTime.now(),
                1,
                true
        );

        mockMvc.perform(post("/notifications/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
        .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().string("Notification saved"));
    }

    @Test
    public void testGetActiveNotifications_Success() throws Exception {
        mockMvc.perform(get("/notifications/active"))
        .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testUpdateSingleNotification_Success() throws Exception {
        mockMvc.perform(put("/notifications/updateOne")
                .param("NotificationId", "1")
                .param("activeFlag", "0"))
                .andExpect(status().isOk())
                .andExpect(content().string("Request Processed Successfully"));
    }

    @Test
    public void testUpdateNotifications_Success() throws Exception {
        List<RequestMapper> notificationRequests = List.of(
                new RequestMapper("1", 0),
                new RequestMapper("2", 1)
        );

        mockMvc.perform(put("/notifications/updateMany")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificationRequests)))
                .andExpect(status().isOk())
                .andExpect(content().string("Request Processed Successfully"));
    }

    @Test
    public void testDeleteNotification_Success() throws Exception {
        mockMvc.perform(delete("/notifications/delete")
                .param("NotificationId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Request Processed Successfully"));
    }
}
