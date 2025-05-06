package com.concurrency.fileprocessing.config;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
public class ShutdownManager {

    private final ThreadPoolTaskExecutor executor;
    private static final Logger logger = LoggerFactory.getLogger(ShutdownManager.class);

    public ShutdownManager(@Qualifier("taskExecutor") ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    @PreDestroy
    public void onShutdown() {
        logger.info("Shutting down...");
        executor.shutdown();
        try {
            if(!executor.getThreadPoolExecutor().awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("Executor shutdown is not working properly...");
            }else {
                logger.info("Shutdown done...");
            }
        }catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Shutdown interrupted", e);
        }
        logger.debug("Shutdown completed");
    }

    @Scheduled(fixedRate = 10000)
    public void logExecutorStats() {
        ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
        logger.info("Active threads: {}", pool.getActiveCount());
        logger.info("Pool size: {}", pool.getPoolSize());
        logger.info("Queue size: {}", pool.getQueue().size());
        logger.info("Completed task count: {}", pool.getCompletedTaskCount());
    }

}
