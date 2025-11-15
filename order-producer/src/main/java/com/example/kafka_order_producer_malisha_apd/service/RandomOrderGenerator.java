package com.example.kafka_order_producer_malisha_apd.service;

import com.example.kafka_order_producer_malisha_apd.model.OrderRequest;
import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "order.producer.auto-generate", name = "enabled", havingValue = "true")
public class RandomOrderGenerator {

    private static final List<String> PRODUCTS = List.of("Book", "Headphones", "Coffee", "Camera", "Keyboard");
    private final SecureRandom random = new SecureRandom();
    private final OrderProducerService orderProducerService;

    @Scheduled(fixedDelayString = "${order.producer.auto-generate.interval-ms:2000}")
    public void publishRandomOrder() {
        OrderRequest request = new OrderRequest();
        request.setProduct(PRODUCTS.get(random.nextInt(PRODUCTS.size())));
        request.setPrice(10 + random.nextDouble(190));
        orderProducerService.publish(request);
        log.debug("Auto-generated order for product {}", request.getProduct());
    }
}
