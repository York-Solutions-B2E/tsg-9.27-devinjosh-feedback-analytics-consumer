# Feedback Analytics Consumer

Spring Boot Kafka consumer that subscribes to the `feedback-submitted` topic and logs received feedback events for analytics purposes.

## Prerequisites
- Java 21
- Docker/Kafka stack from the project `docker-compose.yaml` (or equivalent local Kafka broker)

## Configuration
Environment can be set via `.env` or the Compose stack. Default values are provided in `application.properties` for local/testing use.

| Variable | Default | Description |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker connection string. |
| `KAFKA_CONSUMER_GROUP` | `feedback-analytics-consumer` | Consumer group id override. |
| `KAFKA_SECURITY_PROTOCOL` | `PLAINTEXT` | Optional protocol for secured clusters. |

To run locally with the provided defaults, enable the `local` profile:

```powershell
.\mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Testing
- Full suite: `.\mvnw clean verify`
- Unit tests only: `.\mvnw clean test`
- Focused execution: `.\mvnw clean verify -Dtest=FeedbackEventListenerTest`

Latest run: `.\mvnw clean verify` (Windows PowerShell, Java 21, 2025-11-11) — all tests passed including `FeedbackEventListenerTest` logging assertions and the Jackson round-trip.

Additional run notes are captured alongside the service ADRs in `docs/consumer_day1_notes.md`.
