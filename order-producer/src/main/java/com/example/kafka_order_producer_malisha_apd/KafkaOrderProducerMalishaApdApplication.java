package com.example.kafka_order_producer_malisha_apd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KafkaOrderProducerMalishaApdApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaOrderProducerMalishaApdApplication.class, args);
	}

}
