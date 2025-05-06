package com.concurrency.fileprocessing.metrics;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ThreadPoolMetrics {

    private final ThreadPoolTaskExecutor executor;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    private void registerMetrics() {
                ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();

                Gauge.builder("executor.active.threads", threadPoolExecutor, ThreadPoolExecutor::getActiveCount)
                                .description("Active threads in executor")
                                .register(meterRegistry);

                Gauge.builder("executor.pool.size", threadPoolExecutor, ThreadPoolExecutor::getPoolSize)
                                .description("Current pool size")
                                .register(meterRegistry);

                Gauge.builder("executor.queue.size", threadPoolExecutor, e -> e.getQueue().size())
                                .description("Current pool size")
                                .register(meterRegistry);

                Gauge.builder("executor.completed.tasks", threadPoolExecutor, ThreadPoolExecutor::getCompletedTaskCount)
                                .description("Completed tasks")
                                .register(meterRegistry);

                Gauge.builder("executor.largest.pool.size", threadPoolExecutor, ThreadPoolExecutor::getLargestPoolSize)
                                .description("Largest pool size")
                                .register(meterRegistry);
        }

}
