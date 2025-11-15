package com.example.kafka_order_producer_malisha_apd.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class OrderRequest {

    @NotBlank(message = "Product is required")
    private String product;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private Double price;

    private String orderId;
}
