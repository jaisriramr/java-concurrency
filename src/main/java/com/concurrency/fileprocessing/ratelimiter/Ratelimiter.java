package com.concurrency.fileprocessing.ratelimiter;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Ratelimiter {

    private final StringRedisTemplate redisTemplate;

    public Boolean isAllowed(String key, int maxRequests, int windowInSeconds) {
        String redisKey = "rate_limit:" + key;
        long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if(currentCount == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowInSeconds));
        }

        return currentCount <= maxRequests;

    }

}
