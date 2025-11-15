package com.example.kafka_order_producer_malisha_apd.controller;

import com.example.kafka_order_producer_malisha_apd.model.OrderRequest;
import com.example.kafka_order_producer_malisha_apd.model.OrderResponse;
import com.example.kafka_order_producer_malisha_apd.service.OrderProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderProducerService orderProducerService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderProducerService.publish(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
