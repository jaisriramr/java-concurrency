package com.concurrency.fileprocessing.processor;

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
import org.springframework.web.client.RestTemplate;

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

@Component
@RequiredArgsConstructor
public class WorkerProcessor {

    private final RestTemplate restTemplate;
    private final RetryQueueService retryQueueService;
    private final Ratelimiter ratelimiter;
    private static final Logger logger = LoggerFactory.getLogger(WorkerProcessor.class);

    @Value("${external.api.url}")
    private String targetAPIURL;

    @Value("${external.access.token}")
    private String accessToken;

    @Async("taskExecutor")
    public void processQueue(BlockingQueue<Payload> queue) {
        logger.info("processing queue");
        try {
            while (true) {
                Payload payload = queue.poll(5, TimeUnit.SECONDS);
                
                if (payload == null) {
                    break;
                }

                while (true) {
                    Boolean allowed = ratelimiter.isAllowed("api:taxonomy", 10, 1);

                    if(allowed) {
                        break;
                    }

                    logger.debug("Rate limit exceeded");
                    Thread.sleep(200);
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
            logger.error("Error processing queue: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @TimeLimiter(name = "externalApiLimiter")
    @Retry(name = "externalApiRetry")
    @CircuitBreaker(name = "externalApiBreaker", fallbackMethod = "fallbackApiCall")
    public CompletableFuture<Void> callExternalApi(Payload payload) {
        return CompletableFuture.runAsync(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload.getPayload(), headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(targetAPIURL + "/pim/taxonomy", request, Map.class);

            logger.debug("Processed file response status: {}", response.getStatusCode());
            System.out.println("Processed file response status: " + response.getStatusCode());
        });
    }

    public CompletableFuture<Void> fallbackApiCall(Payload payload, Throwable e) {
        logger.info("Fallback triggered: {}", e.getMessage());
        try {
            try {
                retryQueueService.enqueue(new ObjectMapper().writeValueAsString(payload), 5);
            }catch(JsonProcessingException jsonEx) {
                logger.error("Json processing error: {}", jsonEx);
            }
        }catch(RedisConnectionException ex) {
            logger.error("Redis connection failure during fallback: {}", ex);
        }
        return CompletableFuture.completedFuture(null);
    }


}
