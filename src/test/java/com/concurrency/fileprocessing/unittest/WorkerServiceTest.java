package com.concurrency.fileprocessing.unittest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

import com.concurrency.fileprocessing.metrics.FileProcessingMetrics;
import com.concurrency.fileprocessing.processor.WorkerProcessor;
import com.concurrency.fileprocessing.queue.Payload;
import com.concurrency.fileprocessing.queue.RetryQueueService;
import com.concurrency.fileprocessing.ratelimiter.Ratelimiter;

@ExtendWith(MockitoExtension.class)
public class WorkerServiceTest {

    @Mock 
    private RestTemplate restTemplate;

    @Mock 
    private Ratelimiter rateLimiter;
    
    @Mock 
    private RetryQueueService retryQueueService;

    @InjectMocks 
    private WorkerProcessor workerService;

    @Mock
    private Ratelimiter ratelimiter;

    @Mock
    private FileProcessingMetrics fileProcessingMetrics;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        retryQueueService = mock(RetryQueueService.class);
        ratelimiter = mock(Ratelimiter.class);
        fileProcessingMetrics = mock(FileProcessingMetrics.class);

        workerService = new WorkerProcessor(restTemplate, retryQueueService, ratelimiter, fileProcessingMetrics);
        
        // set private fields
        workerService.getClass().getDeclaredFields(); // can be used for debugging

        setField(workerService, "targetAPIURL", "http://localhost:8080");
        setField(workerService, "accessToken", "dummy-token");
    }

    @Test
    void testCallExternalApi_Success() throws Exception {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test");

        Payload payload = new Payload();
        payload.setPayload(data);

        when(restTemplate.postForEntity(
            anyString(),
            any(HttpEntity.class),
            eq(Map.class)
        )).thenReturn(ResponseEntity.ok(Map.of("status", "success")));

        // Act
        CompletableFuture<Void> future = workerService.callExternalApi(payload);
        future.get();

        // Assert
        verify(restTemplate, times(1)).postForEntity(
            eq("http://localhost:8080/pim/taxonomy"),
            any(HttpEntity.class),
            eq(Map.class)
        );
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }


}
