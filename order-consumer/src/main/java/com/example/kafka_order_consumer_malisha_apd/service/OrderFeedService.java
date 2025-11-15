package com.example.kafka_order_consumer_malisha_apd.service;

import com.example.kafka_order_consumer_malisha_apd.dto.OrderEventDto;
import com.example.kafka_order_consumer_malisha_apd.model.FeedCategory;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OrderFeedService {

    private static final int MAX_EVENTS = 100;
    private final Map<FeedCategory, Deque<OrderEventDto>> feeds = new EnumMap<>(FeedCategory.class);

    public OrderFeedService() {
        for (FeedCategory category : FeedCategory.values()) {
            feeds.put(category, new ArrayDeque<>());
        }
    }

    public synchronized void append(FeedCategory category, OrderEventDto event) {
        Deque<OrderEventDto> deque = feeds.get(category);
        deque.addFirst(event);
        while (deque.size() > MAX_EVENTS) {
            deque.removeLast();
        }
    }

    public synchronized List<OrderEventDto> latest(FeedCategory category) {
        return List.copyOf(feeds.get(category));
    }
}
