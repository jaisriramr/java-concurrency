package com.concurrency.fileprocessing.queue;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetryQueueService {

    private static final String RETRY_QUEUE_KEY = "retry:taxonomy";

    private final StringRedisTemplate redisTemplate;
    
    public void enqueue(String payload, long delayInSeconds) {
        long retryAt = Instant.now().plusSeconds(delayInSeconds).getEpochSecond();
        redisTemplate.opsForZSet().add(RETRY_QUEUE_KEY, payload, retryAt);
    }

    public Set<String> fetchDueRetries() {
        long now = Instant.now().getEpochSecond();
        return redisTemplate.opsForZSet().rangeByScore(RETRY_QUEUE_KEY, 0, now);
    }

    public void remove(String payload) {
        redisTemplate.opsForZSet().remove(RETRY_QUEUE_KEY, payload);
    }

    public long getRetryQueueSize() {
        return redisTemplate.opsForZSet().zCard(RETRY_QUEUE_KEY);
    }

}
