package com.concurrency.fileprocessing.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CSVProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(CSVProcessorService.class);

    public Map<String, Map<String, Object>> process(File file) {

        logger.info("File processing started: {}", file.getName());

        Map<String, Map<String, Object>> result = new HashMap<>();

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            List<String> headers = null;

            while((line = bufferedReader.readLine()) != null) {
                String[] csvData = line.split(",");

                if(headers == null) {
                    headers = new ArrayList<>();
                    for(int i = 1; i < csvData.length; i++) {
                        headers.add(csvData[i].trim());
                        result.put(headers.get(i - 1), new HashMap<>());
                    }
                }else {
                    String rowHeader = csvData[0].trim();

                    for(int i = 1; i < csvData.length; i++) {
                        Map<String, Object> attribute = new HashMap<>();
                        if("1".equals(csvData[i].trim())) {
                            attribute.put("mandatory", true);
                            attribute.put("name", rowHeader);
                            attribute.put("hint", rowHeader);
                            attribute.put("type", "string");
                            attribute.put("options", "");
                            attribute.put("width", 150);
                            attribute.put("isNewColumn", true);
                            attribute.put("wordCount", 0);
                            result.get(headers.get(i - 1)).put(rowHeader, attribute);
                        }
                    }

                }

            }

        }catch(IOException e) {
            logger.error("Error while trying to read the given file: {}", e.getMessage());
            e.printStackTrace();
        }

        System.out.println("count: " + result.size());

        logger.debug("File Processing completed: {} and count: {}", file.getName(), result.size());

        return result;
    }

}
