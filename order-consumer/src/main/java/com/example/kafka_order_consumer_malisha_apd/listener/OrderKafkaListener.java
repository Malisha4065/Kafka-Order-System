package com.example.kafka_order_consumer_malisha_apd.listener;

import com.example.avro.Order;
import com.example.kafka_order_consumer_malisha_apd.dto.OrderEventDto;
import com.example.kafka_order_consumer_malisha_apd.model.FeedCategory;
import com.example.kafka_order_consumer_malisha_apd.service.OrderEventPublisher;
import com.example.kafka_order_consumer_malisha_apd.service.OrderFeedService;
import com.example.kafka_order_consumer_malisha_apd.service.OrderProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaListener {

    private final OrderProcessingService orderProcessingService;
    private final OrderEventPublisher eventPublisher;
    private final OrderFeedService orderFeedService;

    @KafkaListener(topics = "${order.topic.name:orders}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(@Payload Order order,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        OrderEventDto event = orderProcessingService.process(order);
        log.info("Order {} consumed from partition {}. Running average: {} (count={})",
                key, partition, event.average(), event.count());
        orderFeedService.append(FeedCategory.PROCESSED, event);
        eventPublisher.publish(event, FeedCategory.PROCESSED);
    }
}
