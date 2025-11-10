## Consumer Day 1 Notes

### Event Contract (`feedback-submitted`)
- `id`: UUID string (Kafka key and payload field); required.
- `memberId`: string, required, max 36 characters.
- `providerName`: string, required, max 80 characters.
- `rating`: integer 1–5, required.
- `comment`: nullable string, max 200 characters.
- `submittedAt`: ISO 8601 timestamp (UTC), required.
- `schemaVersion`: integer, defaults to `1`, required for forward compatibility.
- Planned POJO: `com.tsg.feedbackconsumer.messaging.FeedbackSubmittedEvent` with nullable `comment`.
- Listener behavior: fail fast (throw/deser error → let Spring/Kafka retry/DLQ if configured) when required fields are missing or type-mismatched; log warn for validation mismatches that pass deserialization (e.g., length overruns).

### Kafka Consumer Configuration Outline
- Topic: `feedback-submitted`.
- Consumer group id: `feedback-analytics-consumer`.
- Key deserializer: `org.apache.kafka.common.serialization.StringDeserializer`.
- Value deserializer: `org.springframework.kafka.support.serializer.JsonDeserializer`.
- JSON trusted packages: `com.tsg.feedbackconsumer.messaging`.
- Default POJO binding: `com.tsg.feedbackconsumer.messaging.FeedbackSubmittedEvent`.
- Bootstrap servers env var: `KAFKA_BOOTSTRAP_SERVERS` (default `localhost:9092`).
- Additional env vars to plan:
  - `KAFKA_SECURITY_PROTOCOL` (future-proof, default `PLAINTEXT`).
  - `KAFKA_CONSUMER_GROUP` (optional override).
- Draft `application-local.yml`:

```
spring:
  application:
    name: feedback-analytics-consumer
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: ${KAFKA_CONSUMER_GROUP:feedback-analytics-consumer}
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.value.default.type: com.tsg.feedbackconsumer.messaging.FeedbackSubmittedEvent
        spring.json.trusted.packages: com.tsg.feedbackconsumer.messaging
    listener:
      ack-mode: record
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

- Logging plan: use `application.yml` profile section to set `logging.level.com.tsg.feedbackconsumer=INFO`.

### Project Structure & Responsibilities
- `src/main/java/com/tsg/feedbackconsumer/`
  - `FeedbackConsumerApplication.java` (Spring Boot entry point).
  - `controllers/HealthController.java`: expose `GET /health`, return `{ "status": "UP" }`.
  - `messaging/KafkaConsumerConfig.java`: configure consumer factory, listener container factory, JSON deserializer, error handler (with logging).
  - `messaging/FeedbackSubmittedEvent.java`: immutable POJO (record or class) mirroring event contract.
  - `messaging/FeedbackEventListener.java`: `@KafkaListener` on topic, logs structured message.
  - `logging/StructuredLogger.java` (optional helper if needed).
- `src/test/java/com/tsg/feedbackconsumer/`
  - `messaging/FeedbackEventListenerTest.java`: unit tests using `@ExtendWith(MockitoExtension.class)` verifying logging on valid payload, error handling on invalid payload.
  - `messaging/KafkaConsumerConfigTest.java`: (optional) ensures bean wiring, trusted packages configured.

### Testing & Observability Plan
- Unit tests stub Kafka using `KafkaTemplate` mocks or `org.springframework.kafka.support.Acknowledgment`.
- Consider contract test for JSON mapping (Jackson `ObjectMapper` round-trip).
- Provide structured logs via `Logger.info("Received feedback", kv...)` or `log.info("Received feedback ...")` with MDC fields.
- Health endpoint leveraged by Docker Compose health check; eventual `Dockerfile` will run `./gradlew bootJar`.

### Open Questions / Day 2 Follow-ups
- Confirm structured logging format (JSON encoder vs. key/value message).
- Decide on error handling policy (simple log + retry, or configure DLQ).
- Verify build tool selection (Gradle vs. Maven) before generating project scaffold.


