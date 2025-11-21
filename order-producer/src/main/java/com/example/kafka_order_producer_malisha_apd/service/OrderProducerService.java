package com.example.kafka_order_producer_malisha_apd.service;

import com.example.avro.Order;
import com.example.kafka_order_producer_malisha_apd.exception.OrderPublishException;
import com.example.kafka_order_producer_malisha_apd.model.OrderRequest;
import com.example.kafka_order_producer_malisha_apd.model.OrderResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducerService {

    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Value("${order.topic.name:orders}")
    private String ordersTopic;

    public OrderResponse publish(OrderRequest request) {
        Order order = buildOrderPayload(request);
        sendWithRetry(order);
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .product(order.getProduct())
                .price(order.getPrice())
                .status("PUBLISHED")
                .build();
    }

    private void sendWithRetry(Order order) {
        try {
            log.info("Sending order {}", order.getOrderId());
            
            CompletableFuture<SendResult<String, Order>> future = kafkaTemplate.send(ordersTopic, order.getOrderId(), order);
            
            future.get();
            
            log.info("Order {} sent successfully", order.getOrderId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderPublishException("Order thread interrupted " + order.getOrderId(), e);
        } catch (ExecutionException e) {
            throw new OrderPublishException("Failed to publish order %s after retries".formatted(order.getOrderId()), e.getCause());
        } catch (Exception e) {
            throw new OrderPublishException("Unexpected error publishing order %s".formatted(order.getOrderId()), e);
        }
    }

    private Order buildOrderPayload(OrderRequest request) {
        String orderId = request.getOrderId() != null ? request.getOrderId() : UUID.randomUUID().toString();
        return Order.newBuilder()
                .setOrderId(orderId)
                .setProduct(request.getProduct())
                .setPrice(request.getPrice().floatValue())
                .build();
    }
}