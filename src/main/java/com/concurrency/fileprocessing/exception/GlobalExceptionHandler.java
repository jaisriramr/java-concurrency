package com.concurrency.fileprocessing.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<String> handleBadRequest(Exception e) {
        logger.error("Bad request: {}", e.getMessage());

        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<String> handleInternalError(Exception e) {
        logger.error("Internal server error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
    }
}
