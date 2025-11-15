# Kafka Order System

Two Spring Boot microservices showcase an end-to-end Kafka solution using Avro serialization, real-time aggregation, retries, and a dead-letter queue (DLQ). The producer exposes an HTTP API (and optional auto-generator) that emits order events to Confluent Cloud (or local Kafka). The consumer processes the events, maintains a running average of order prices, retries temporary failures with exponential backoff, and forwards permanent failures to a DLQ.

## Architecture

- **Topics**
  - `orders`: primary topic for order events (key = `orderId`).
  - `orders-dlq`: DLQ topic populated by the consumer’s `DeadLetterPublishingRecoverer` after retry exhaustion or fatal errors.
- **Serialization**: Both services share the Avro schema defined in `src/main/avro/order.avsc`. Spring Kafka uses Confluent's Avro serializer/deserializer configured via `application.properties`.
- **Producer**
  - REST endpoint `POST /api/orders` accepts `{product, price}` and publishes to Kafka.
  - `RetryTemplate` wraps the send to handle transient broker issues.
  - Optional scheduled generator (`order.producer.auto-generate.enabled=true`) emits random orders for demos.
- **Consumer**
  - `DefaultErrorHandler` + `ExponentialBackOffWithMaxRetries` handles retries.
  - `DeadLetterPublishingRecoverer` publishes failed records to DLQ.
  - `RunningAverageTracker` maintains running totals to compute a live average after each successful message.
  - `DlqListener` logs DLQ traffic for observability.

## Prerequisites

- Java 21+
- Maven 3.9+
- Kafka cluster (Confluent Cloud recommended) and Schema Registry

## Configuration

Both applications read settings from environment variables so you can target Confluent Cloud without editing code.

| Environment Variable | Description |
| --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | e.g. `pkc-xxxxx.us-central1.gcp.confluent.cloud:9092` |
| `KAFKA_SECURITY_PROTOCOL` | Usually `SASL_SSL` for Confluent Cloud (defaults to `PLAINTEXT` for local dev) |
| `KAFKA_SASL_MECHANISM` | Typically `PLAIN` |
| `KAFKA_API_KEY` / `KAFKA_API_SECRET` | Cluster API key/secret |
| `KAFKA_SASL_JAAS_CONFIG` | Optional override of the JAAS config string |
| `SCHEMA_REGISTRY_URL` | e.g. `https://psrc-xxxxx.us-central1.gcp.confluent.cloud` |
| `SCHEMA_REGISTRY_API_KEY` / `SCHEMA_REGISTRY_API_SECRET` | Schema Registry key & secret |
| `SCHEMA_REGISTRY_AUTH_SOURCE` | Usually `USER_INFO` |

Topic names, retry knobs, and generator settings live under the `order.*` namespace inside each `application.properties` if further customization is needed.

> **Tip:** If you are running against a single-broker local cluster, set `order.topic.replication-factor=1` and keep `order.topic.auto-create.enabled=true` so that topics are created with compatible settings. Set it to `false` if you prefer to manage topics yourself (useful during tests).

## Running Locally

1. **Generate Avro classes** (from repo root):
   ```bash
   ./mvnw -pl order-producer,order-consumer clean compile
   ```

2. **Start the producer**:
   ```bash
   cd order-producer
   ./mvnw spring-boot:run
   ```

3. **Create an order**:
   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H 'Content-Type: application/json' \
     -d '{"product":"Camera","price":199.99}'
   ```

4. **Start the consumer** (new terminal):
   ```bash
   cd order-consumer
   ./mvnw spring-boot:run
   ```

Watch the consumer logs to see the running average and retry/DLQ behavior. Enable auto-generation by setting `order.producer.auto-generate.enabled=true` (e.g., via env var or properties) to stream random data continuously.

## Testing & Validation

- `order-consumer` logs show retries and DLQ transfers; the DLQ listener outputs root causes.
- The producer’s REST endpoint returns an order id and status when publishing succeeds; `RestExceptionHandler` surfaces errors if Kafka is unreachable.
- `RandomOrderGenerator` plus the consumer running average provide a live demo without extra tooling.

## Next Steps

- Swap random failure simulation with real validation/business rules.
