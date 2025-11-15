package com.example.kafka_order_consumer_malisha_apd.controller;

import com.example.kafka_order_consumer_malisha_apd.dto.OrderEventDto;
import com.example.kafka_order_consumer_malisha_apd.model.FeedCategory;
import com.example.kafka_order_consumer_malisha_apd.service.OrderFeedService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class OrderFeedController {

    private final OrderFeedService orderFeedService;

    @GetMapping
    public List<OrderEventDto> latest(@RequestParam(name = "category", defaultValue = "PROCESSED") FeedCategory category) {
        return orderFeedService.latest(category);
    }
}
