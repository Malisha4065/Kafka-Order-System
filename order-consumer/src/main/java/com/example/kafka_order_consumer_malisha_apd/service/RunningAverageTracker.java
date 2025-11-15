package com.example.kafka_order_consumer_malisha_apd.service;

import com.example.kafka_order_consumer_malisha_apd.model.RunningAverageMetrics;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Component;

@Component
public class RunningAverageTracker {

    private final DoubleAdder total = new DoubleAdder();
    private final LongAdder count = new LongAdder();

    public RunningAverageMetrics update(double price) {
        total.add(price);
        count.increment();
        return getCurrentMetrics();
    }

    public RunningAverageMetrics getCurrentMetrics() {
        double totalValue = total.sum();
        long currentCount = count.sum();
        double average = currentCount == 0 ? 0 : totalValue / currentCount;
        return new RunningAverageMetrics(currentCount, totalValue, average);
    }
}
