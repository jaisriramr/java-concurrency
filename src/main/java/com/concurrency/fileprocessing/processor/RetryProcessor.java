package com.concurrency.fileprocessing.processor;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.concurrency.fileprocessing.queue.Payload;
import com.concurrency.fileprocessing.queue.RetryQueueService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RetryProcessor {

    private final RetryQueueService retryQueueService;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RetryProcessor.class);

    @Value("${external.api.url}")
    private String targetAPIURL;

    @Value("${external.access.token}")
    private String accessToken;

    @Scheduled(fixedDelay = 2000)
    public void poll() {
        Set<String> retries = retryQueueService.fetchDueRetries();

        for(String payload: retries) {
            try {
                Payload requestPayload = new ObjectMapper().readValue(payload, Payload.class);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(accessToken);

                HttpEntity<Map<String, Object>> httprequest = new HttpEntity<>(requestPayload.getPayload(),headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(targetAPIURL + "/pim/taxonomy", httprequest, Map.class);

                logger.debug("Retry process response status: {}", response.getStatusCode());
                System.out.println("Retry process response status: " + response.getStatusCode());

                retryQueueService.remove(payload);
            }catch(Exception ex) {
                logger.error("Retry failed", ex);
            }
        }

    }


}
