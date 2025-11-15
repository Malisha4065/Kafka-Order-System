package com.example.kafka_order_consumer_malisha_apd.exception;

public class TransientOrderException extends RuntimeException {
    public TransientOrderException(String message) {
        super(message);
    }
}
