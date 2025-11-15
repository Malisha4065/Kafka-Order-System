package com.example.kafka_order_consumer_malisha_apd.service;

import com.example.kafka_order_consumer_malisha_apd.dto.OrderEventDto;
import com.example.kafka_order_consumer_malisha_apd.model.FeedCategory;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OrderFeedService {

    private static final int MAX_EVENTS = 100;
    private static final EnumSet<FeedCategory> STORED_FEEDS = EnumSet.of(FeedCategory.PROCESSED, FeedCategory.DLQ);
    private static final Comparator<OrderEventDto> EVENT_ORDER = Comparator
            .comparing(OrderEventDto::timestamp, Comparator.nullsLast(Comparator.naturalOrder()))
            .reversed();
    private final Map<FeedCategory, Deque<OrderEventDto>> feeds = new EnumMap<>(FeedCategory.class);

    public OrderFeedService() {
        STORED_FEEDS.forEach(category -> feeds.put(category, new ArrayDeque<>()));
    }

    public synchronized void append(FeedCategory category, OrderEventDto event) {
        Deque<OrderEventDto> deque = feeds.get(category);
        if (deque == null) {
            return;
        }
        deque.addFirst(event);
        while (deque.size() > MAX_EVENTS) {
            deque.removeLast();
        }
    }

    public synchronized List<OrderEventDto> latest(FeedCategory category) {
        if (category == FeedCategory.ALL) {
            return feeds.values().stream()
                    .flatMap(Deque::stream)
                    .sorted(EVENT_ORDER)
                    .limit(MAX_EVENTS)
                    .toList();
        }
        Deque<OrderEventDto> deque = feeds.get(category);
        return deque == null ? List.of() : List.copyOf(deque);
    }
}
