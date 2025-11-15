package com.example.kafka_order_consumer_malisha_apd.listener;

import com.example.avro.Order;
import com.example.kafka_order_consumer_malisha_apd.model.RunningAverageMetrics;
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

    @KafkaListener(topics = "${order.topic.name:orders}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(@Payload Order order,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        RunningAverageMetrics metrics = orderProcessingService.process(order);
        log.info("Order {} consumed from partition {}. Running average: {} (count={})",
                key, partition, metrics.average(), metrics.count());
    }
}
