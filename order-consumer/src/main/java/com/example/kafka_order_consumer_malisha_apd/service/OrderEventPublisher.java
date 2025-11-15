package com.example.kafka_order_consumer_malisha_apd.service;

import com.example.kafka_order_consumer_malisha_apd.dto.OrderEventDto;
import com.example.kafka_order_consumer_malisha_apd.model.FeedCategory;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private static final Map<FeedCategory, String> DESTINATIONS = Map.of(
            FeedCategory.PROCESSED, "/topic/orders/processed",
            FeedCategory.DLQ, "/topic/orders/dlq"
    );

    public void publish(OrderEventDto event, FeedCategory category) {
        messagingTemplate.convertAndSend(DESTINATIONS.get(category), event);
        log.debug("Published {} order event {}", category, event.orderId());
    }

    public void publish(OrderEventDto event) {
        publish(event, FeedCategory.PROCESSED);
    }
}
