package com.example.kafka_order_consumer_malisha_apd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "order.topic.auto-create.enabled=false")
class KafkaOrderConsumerMalishaApdApplicationTests {

	@Test
	void contextLoads() {
	}

}
