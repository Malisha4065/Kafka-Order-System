package com.example.kafka_order_consumer_malisha_apd.listener;

import com.example.avro.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DlqListener {

    @KafkaListener(topics = "${order.topic.dlq:orders-dlq}", groupId = "${spring.kafka.consumer.group-id}-dlq")
    public void consumeDlq(@Payload Order order,
                           @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
                           @Header(value = KafkaHeaders.DLT_EXCEPTION_FQCN, required = false) String exceptionClassName) {
        log.error("Order {} moved to DLQ due to {}: {}", order.getOrderId(), exceptionClassName, exceptionMessage);
    }
}
