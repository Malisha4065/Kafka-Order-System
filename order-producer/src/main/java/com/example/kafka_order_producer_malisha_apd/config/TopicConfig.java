package com.example.kafka_order_producer_malisha_apd.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(prefix = "order.topic.auto-create", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TopicConfig {

    @Bean
    public NewTopic ordersTopic(@Value("${order.topic.name:orders}") String ordersTopicName,
                                @Value("${order.topic.partitions:3}") int partitions,
                                @Value("${order.topic.replication-factor:3}") short replicationFactor) {
    return TopicBuilder.name(ordersTopicName)
        .partitions(partitions)
        .replicas(replicationFactor)
        .build();
    }

    @Bean
    public NewTopic ordersDlqTopic(@Value("${order.topic.dlq:orders-dlq}") String dlqTopicName,
                                   @Value("${order.topic.partitions:3}") int partitions,
                                   @Value("${order.topic.replication-factor:3}") short replicationFactor) {
    return TopicBuilder.name(dlqTopicName)
        .partitions(partitions)
        .replicas(replicationFactor)
        .build();
    }
}
