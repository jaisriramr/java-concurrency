package com.concurrency.fileprocessing.metrics;

import java.time.Duration;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class FileProcessingMetrics {

    private final Counter successCounter;
    private final Counter failureCounter;
    private final Timer processingTimer;
    private final Counter totalCounter;

    public FileProcessingMetrics(MeterRegistry meterRegistry) {
        this.successCounter = meterRegistry.counter("file.processing.success");
        this.failureCounter = meterRegistry.counter("file.processing.failure");
        this.processingTimer = meterRegistry.timer("file.processing.duration");
        this.totalCounter = meterRegistry.counter("file.processing.total");

    }

    public void recordSuccess(Duration duration) {
        successCounter.increment();
        totalCounter.increment();
        processingTimer.record(duration);
    }

    public void recordFailure() {
        failureCounter.increment();
    }

}
