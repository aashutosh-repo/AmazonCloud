package com.test.microservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.microservices.entity.Alerts;
import com.test.microservices.mapper.ObjectsMapper;
import com.test.microservices.services.AlertService;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private S3Client client;

    @Mock
    private ObjectsMapper<Alerts> mapper;

    @InjectMocks
    private AlertService alertService;

    private final String bucketName = "test-bucket";
    Alerts alert;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(client,bucketName,mapper);
        alert = new Alerts("1", "Test Alert", "Test Message",LocalDateTime.now(), 1);
    }
    
    @Test
    void testSaveAlert_Success() throws JsonProcessingException, JsonProcessingException {
        
        String alertJson = "{\"alertId\":\"1\"}";

        when(mapper.toJson(alert)).thenReturn(alertJson);

        alertService.saveAlert(alert);

        verify(client, times(1)).putObject(
            any(PutObjectRequest.class),
            any(RequestBody.class)
        );
    }
    @Test
    void testGetActiveNotifications_Exception() {
        when(client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(S3Exception.builder().message("S3 Error").build());

        assertThrows(S3Exception.class, () -> alertService.getActiveNotifications());
    }
    
    @Test
    void testGetActiveNotifications_NoActiveNotifications() throws JsonProcessingException {
        // Arrange
        String alertKey = "Alert/123.json";
        String alertJson = "{\"alertId\":\"123\", \"title\":\"Title\", \"message\":\"Test Message\", \"timestamp\":\"2024-12-21T10:15:30\", \"isActive\":1}";

        // Mock S3Object list response
        List<S3Object> s3Objects = List.of(S3Object.builder().key(alertKey).build());
        ListObjectsV2Response listResponse = ListObjectsV2Response.builder()
                .contents(s3Objects)
                .isTruncated(false)
                .build();

        // Mock S3 client for listing objects
        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResponse);

        // Mock S3 client for getting object content as bytes
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(
                GetObjectResponse.builder().build(),
                alertJson.getBytes()
        );
        when(client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);
        
        // Act
        List<Alerts> result = alertService.getActiveNotifications();

        // Assert
        assertFalse(result.isEmpty()); // Since `isActive` is 0, the list should be empty
        assertEquals(1, result.size());
        verify(client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
     }


}
