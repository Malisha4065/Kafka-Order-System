package com.example.kafka_order_consumer_malisha_apd.service;

import com.example.kafka_order_consumer_malisha_apd.dto.OrderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(OrderEventDto event) {
        messagingTemplate.convertAndSend("/topic/orders", event);
        log.debug("Published order event {}", event.orderId());
    }
}
