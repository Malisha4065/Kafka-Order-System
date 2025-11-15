package com.example.kafka_order_consumer_malisha_apd.service;

import com.example.kafka_order_consumer_malisha_apd.dto.OrderEventDto;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderFeedService {

    private static final int MAX_EVENTS = 100;
    private final Deque<OrderEventDto> events = new ArrayDeque<>();

    public synchronized void append(OrderEventDto event) {
        events.addFirst(event);
        while (events.size() > MAX_EVENTS) {
            events.removeLast();
        }
    }

    public synchronized List<OrderEventDto> latest() {
        return events.stream().toList();
    }
}
