package com.example.kafka_order_producer_malisha_apd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String product;
    private double price;
    private String status;
}
