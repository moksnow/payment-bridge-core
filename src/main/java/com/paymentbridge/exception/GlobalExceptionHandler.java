package com.paymentbridge.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/*
 * @author Moh Khandan
 * Date: 06/12/2026
 * Time: 16:37 PM
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(Map.of(
                        "code",      ex.getErrorCode(),
                        "message",   ex.getMessage(),
                        "timestamp", Instant.now().toString()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity
                .internalServerError()
                .body(Map.of(
                        "code",      "INTERNAL_ERROR",
                        "message",   "An unexpected error occurred",
                        "timestamp", Instant.now().toString()
                ));
    }
}
