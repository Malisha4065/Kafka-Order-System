package com.example.kafka_order_consumer_malisha_apd.service;

import com.example.avro.Order;
import com.example.kafka_order_consumer_malisha_apd.dto.OrderEventDto;
import com.example.kafka_order_consumer_malisha_apd.exception.FatalOrderException;
import com.example.kafka_order_consumer_malisha_apd.exception.TransientOrderException;
import com.example.kafka_order_consumer_malisha_apd.model.RunningAverageMetrics;
import java.security.SecureRandom;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final RunningAverageTracker runningAverageTracker;
    private final SecureRandom random = new SecureRandom();

    @Value("${order.consumer.failure.fatal-threshold:250}")
    private double fatalThreshold;

    @Value("${order.consumer.failure.transient-probability:0.1}")
    private double transientFailureProbability;

    public OrderEventDto process(Order order) {
        double price = order.getPrice();

        if (price >= fatalThreshold) {
            throw new FatalOrderException("Price %.2f exceeds fatal threshold %.2f".formatted(price, fatalThreshold));
        }

        if (random.nextDouble() < transientFailureProbability) {
            throw new TransientOrderException("Simulated transient failure for order %s".formatted(order.getOrderId()));
        }

    RunningAverageMetrics metrics = runningAverageTracker.update(price);
    log.info("Processed order {} (product: {}) -> count={}, avg={}",
        order.getOrderId(), order.getProduct(), metrics.count(), metrics.average());
    return new OrderEventDto(
        order.getOrderId(),
        order.getProduct(),
        price,
        Instant.now(),
        metrics.count(),
        metrics.average(),
        "PROCESSED",
        null
    );
    }
}
