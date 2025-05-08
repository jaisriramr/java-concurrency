package com.concurrency.fileprocessing.processor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.concurrency.fileprocessing.metrics.FileProcessingMetrics;
import com.concurrency.fileprocessing.queue.Payload;
import com.concurrency.fileprocessing.queue.RetryQueueService;
import com.concurrency.fileprocessing.ratelimiter.Ratelimiter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkerProcessor {

    private final RestTemplate restTemplate;
    private final RetryQueueService retryQueueService;
    private final Ratelimiter ratelimiter;
    private final FileProcessingMetrics fileProcessingMetrics;
    private static final Logger logger = LoggerFactory.getLogger(WorkerProcessor.class);
    private Integer count = 0;

    @Value("${external.api.url}")
    private String targetAPIURL;

    @Value("${external.access.token}")
    private String accessToken;

    @Async("taskExecutor")
    public void processQueue(BlockingQueue<Payload> queue) {
        logger.info("processing queue: {}", queue.size());
        
        try {
            while (true) {
                // Payload payload = queue.poll(5, TimeUnit.SECONDS);
                Payload payload = queue.take();
                
                if (payload == null) {
                    logger.info("Queue is empty for 5 seconds. Ending processing...");
                    break;
                }

                while (!ratelimiter.isAllowed("api:taxonomy", 500, 5)) {
                    logger.debug("Rate limit exceeded...");
                    Thread.sleep(500);
                }

                try {
                    callExternalApi(payload);
                }catch(Exception e) {
                    try {
                        retryQueueService.enqueue(new ObjectMapper().writeValueAsString(payload), 5);
                    }catch(RedisConnectionFailureException ex) {
                        logger.error("Redis connection failed: {}", ex);
                    }
                    e.printStackTrace();
                }

            }
        }catch(Exception e) {
            fileProcessingMetrics.recordFailure();
            logger.error("Error processing queue: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @TimeLimiter(name = "externalApiLimiter")
    @Retry(name = "externalApiRetry")
    @CircuitBreaker(name = "externalApiBreaker", fallbackMethod = "fallbackApiCall")
    public CompletableFuture<Void> callExternalApi(Payload payload) {
        return CompletableFuture.runAsync(() -> {
            count++;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload.getPayload(), headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(targetAPIURL + "/pim/taxonomy", request, Map.class);

            logger.debug("Log Processed file response status: {}", response.getStatusCode());
            System.out.println("Processed file response status: " + response.getStatusCode() + " count: " + count + " dd " + payload.getPayload().get("name"));
            fileProcessingMetrics.recordSuccess(null);
            // System.out.println("HTTPCOUNT: " + count);
        });
    }

    public CompletableFuture<Void> fallbackApiCall(Payload payload, Throwable e) {
        logger.info("Fallback triggered: {}", e.getMessage());
        try {
            try {
                retryQueueService.enqueue(new ObjectMapper().writeValueAsString(payload), 5);
            }catch(JsonProcessingException jsonEx) {
                fileProcessingMetrics.recordFailure();
                logger.error("Json processing error: {}", jsonEx);
            }
        }catch(RedisConnectionException ex) {
            fileProcessingMetrics.recordFailure();
            logger.error("Redis connection failure during fallback: {}", ex);
        }
        return CompletableFuture.completedFuture(null);
    }


}
