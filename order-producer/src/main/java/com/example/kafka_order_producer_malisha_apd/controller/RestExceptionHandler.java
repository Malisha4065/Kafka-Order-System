package com.example.kafka_order_producer_malisha_apd.controller;

import com.example.kafka_order_producer_malisha_apd.exception.OrderPublishException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(OrderPublishException.class)
    public ResponseEntity<Map<String, Object>> handlePublishException(OrderPublishException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "timestamp", Instant.now().toString(),
                "error", "Failed to publish order",
                "details", exception.getMessage()
        ));
    }
}
