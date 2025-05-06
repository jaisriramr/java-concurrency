package com.concurrency.fileprocessing.processor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.concurrency.fileprocessing.queue.Payload;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MainProcessor {

    private final WorkerProcessor workerProcessor;
    private final CSVProcessorService csvProcessorService;
    private static final Logger logger = LoggerFactory.getLogger(MainProcessor.class);

    public void process(File file) {
        logger.info("Main processor initiated");
        BlockingQueue<Payload> queue = new LinkedBlockingQueue<>(100);

        Map<String, Map<String, Object>> result = csvProcessorService.process(file);
        
        for(int i = 0; i < 10; i++) {
            logger.debug("Worker processor initiated: {}", i);
            workerProcessor.processQueue(queue);
        }

        result.forEach((column, rowName) -> {
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("generic", false);
            requestPayload.put("name", column);
            requestPayload.put("description", column);
            requestPayload.put("image", null);
            requestPayload.put("attributes", rowName);
            try{
                queue.offer(new Payload(requestPayload), 1000, TimeUnit.MILLISECONDS);
            }catch(InterruptedException e) {
                logger.error("Error in offering payload to the queue: {}", e);
                e.printStackTrace();
            }
        });

    }
    
}
