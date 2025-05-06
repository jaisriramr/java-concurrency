package com.concurrency.fileprocessing.metrics;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.concurrency.fileprocessing.queue.RetryQueueService;

import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RetryQueueMetrics {

    private final RetryQueueService retryQueueService;

    @Bean
    public MeterBinder retryQueueSizeMetrics() {
        return registry -> registry.gauge("fileprocessing_retry_queue_size", retryQueueService, RetryQueueService::getRetryQueueSize);
    }

}
