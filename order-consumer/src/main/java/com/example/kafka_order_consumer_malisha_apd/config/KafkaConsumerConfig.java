package com.example.kafka_order_consumer_malisha_apd.config;

import com.example.avro.Order;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import com.example.kafka_order_consumer_malisha_apd.exception.FatalOrderException;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, Order> orderConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, Order> orderDlqProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.putIfAbsent(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Order> dlqKafkaTemplate(ProducerFactory<String, Order> orderDlqProducerFactory) {
        return new KafkaTemplate<>(orderDlqProducerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Order> kafkaListenerContainerFactory(
            ConsumerFactory<String, Order> orderConsumerFactory,
            KafkaTemplate<String, Order> dlqKafkaTemplate,
            @Value("${order.topic.dlq:orders-dlq}") String dlqTopic,
            @Value("${order.consumer.retry.max-attempts:3}") int maxAttempts,
            @Value("${order.consumer.retry.initial-interval:500}") long initialInterval,
            @Value("${order.consumer.retry.multiplier:2.0}") double multiplier,
            @Value("${order.consumer.retry.max-interval:5000}") long maxInterval) {

        ConcurrentKafkaListenerContainerFactory<String, Order> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderConsumerFactory);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(dlqKafkaTemplate,
                (record, exception) -> new TopicPartition(dlqTopic, record.partition()));

        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(Math.max(0, maxAttempts - 1));
        backoff.setInitialInterval(initialInterval);
        backoff.setMultiplier(multiplier);
        backoff.setMaxInterval(maxInterval);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backoff);
        errorHandler.addNotRetryableExceptions(FatalOrderException.class);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
