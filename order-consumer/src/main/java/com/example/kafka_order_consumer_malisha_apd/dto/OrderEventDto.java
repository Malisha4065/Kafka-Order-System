package com.example.kafka_order_consumer_malisha_apd.dto;

import java.time.Instant;

public record OrderEventDto(
        String orderId,
        String product,
        double price,
        Instant timestamp,
        long count,
        double average,
        String status,
        String message
) {}
