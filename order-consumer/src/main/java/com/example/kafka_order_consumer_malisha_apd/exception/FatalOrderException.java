package com.example.kafka_order_consumer_malisha_apd.exception;

public class FatalOrderException extends RuntimeException {
    public FatalOrderException(String message) {
        super(message);
    }
}
