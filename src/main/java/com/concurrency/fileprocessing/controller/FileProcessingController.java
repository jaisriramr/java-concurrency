package com.concurrency.fileprocessing.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.concurrency.fileprocessing.processor.MainProcessor;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileProcessingController {

    private final MainProcessor processor;

    @PostMapping("/process-csv")
    public ResponseEntity<String> processCSV(@RequestParam("file") MultipartFile file) throws IOException {
        
        if(file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        if(file.getContentType() != "text/csv") {
            return ResponseEntity.badRequest().body("Only CSV file type is allowed");
        }


        File tempFile = File.createTempFile("uploaded-", ".csv");
        file.transferTo(tempFile);

        processor.process(tempFile);

        return ResponseEntity.ok("File Processing Started!");

    }
    

}
