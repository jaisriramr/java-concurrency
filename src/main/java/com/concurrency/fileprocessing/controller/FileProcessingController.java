package com.concurrency.fileprocessing.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.concurrency.fileprocessing.processor.MainProcessor;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileProcessingController {

    private final MainProcessor processor;

    @PostMapping("/process-csv")
    public ResponseEntity<?> processCSV(@RequestParam("files") MultipartFile[] files) throws IOException {

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("No files uploaded");
        }

        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                failedFiles.add(file.getOriginalFilename() + " (empty)");
                continue;
            }

            if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx")) {
                return ResponseEntity.badRequest().body("Only .xlsx files are allowed");
            }

            try {
                File tempFile = File.createTempFile("uploaded-", ".xlsx");
                file.transferTo(tempFile);
    
                processor.process(tempFile);
                // CompletableFuture.runAsync(() -> {
                //     try {
                //         processor.process(tempFile);
                //     }finally {
                //         if (tempFile.exists()) {
                //             tempFile.delete();
                //         }
                //     }
                // });
    
                tempFile.delete();
            }catch(IOException e) {
                failedFiles.add(file.getOriginalFilename() + " (error: " + e.getMessage() + ")");
            }

        }

        if (failedFiles.isEmpty()) {
            return ResponseEntity.ok("All files are being processed asynchronously.");
        } else {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                    .body("Some files failed: " + String.join(", ", failedFiles));
        }
    }

}
