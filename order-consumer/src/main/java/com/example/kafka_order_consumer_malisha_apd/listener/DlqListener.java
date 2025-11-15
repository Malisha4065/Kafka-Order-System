package com.example.kafka_order_consumer_malisha_apd.listener;

import com.example.avro.Order;
import com.example.kafka_order_consumer_malisha_apd.dto.OrderEventDto;
import com.example.kafka_order_consumer_malisha_apd.model.RunningAverageMetrics; // Import this
import com.example.kafka_order_consumer_malisha_apd.service.OrderEventPublisher;
import com.example.kafka_order_consumer_malisha_apd.service.OrderFeedService;
import com.example.kafka_order_consumer_malisha_apd.service.RunningAverageTracker; // Import this
import java.time.Instant;
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
public class DlqListener {

    private final OrderFeedService orderFeedService;
    private final OrderEventPublisher eventPublisher;
    private final RunningAverageTracker runningAverageTracker;

    @KafkaListener(topics = "${order.topic.dlq:orders-dlq}", groupId = "${spring.kafka.consumer.group-id}-dlq")
    public void consumeDlq(@Payload Order order,
                           @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
                           @Header(value = KafkaHeaders.DLT_EXCEPTION_FQCN, required = false) String exceptionClassName) {
        log.error("Order {} moved to DLQ due to {}: {}", order.getOrderId(), exceptionClassName, exceptionMessage);

        RunningAverageMetrics metrics = runningAverageTracker.getCurrentMetrics();

        OrderEventDto event = new OrderEventDto(
                order.getOrderId(),
                order.getProduct(),
                order.getPrice(),
                Instant.now(),
                metrics.count(),
                metrics.average(),
                "FAILED",
                exceptionMessage != null ? exceptionMessage : "Unknown error"
        );
        orderFeedService.append(event);
        eventPublisher.publish(event);
    }
}
