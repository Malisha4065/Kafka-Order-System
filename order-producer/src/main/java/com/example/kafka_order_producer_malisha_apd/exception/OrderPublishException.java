package com.example.kafka_order_producer_malisha_apd.exception;

public class OrderPublishException extends RuntimeException {

    public OrderPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
