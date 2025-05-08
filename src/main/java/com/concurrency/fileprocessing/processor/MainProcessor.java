package com.concurrency.fileprocessing.processor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.concurrency.fileprocessing.queue.Payload;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MainProcessor {

    private final WorkerProcessor workerProcessor;
    private final XLSXProcessorService xlsxProcessorService;
    private static final Logger logger = LoggerFactory.getLogger(MainProcessor.class);
    
    BlockingQueue<Payload> queue = new LinkedBlockingQueue<>(10000);

    @PostConstruct
    public void init() {
        logger.info("Starting async worker consumers...");
        for (int i = 0; i < 40; i++) {
            CompletableFuture.runAsync(() -> workerProcessor.processQueue(queue));
        }
    }

    public void process(File file) {
        logger.info("Main processor initiated Queue size: "  );

        Map<String, Map<String, Object>> result = xlsxProcessorService.process(file);

        result.forEach((column, rowName) -> {
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("generic", false);
            requestPayload.put("name", column);
            requestPayload.put("description", column);
            requestPayload.put("image", null);
            requestPayload.put("attributes", rowName);
            try{
                queue.put(new Payload(requestPayload));
            }catch(InterruptedException e) {
                logger.error("Error in offering payload to the queue: {}", e);
                e.printStackTrace();
            }
        });

    }
    
}
