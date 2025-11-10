# Feedback Analytics Consumer

Spring Boot Kafka consumer that subscribes to the `feedback-submitted` topic and logs received feedback events for analytics purposes.

## Day 1 Status
- Planning notes captured in `docs/consumer_day1_notes.md` (event contract, configuration plan, package layout, testing strategy).
- Pending decisions: structured logging format, error-handling/DLQ approach, build tool choice (Gradle vs. Maven).

## Tooling Baseline
- Java 17+ (Spring Boot scaffold to be generated Day 2).
- Formatter/lint/test scripts will align with other repos (`./gradlew spotlessApply`, `./gradlew test`) once the project skeleton is generated.
- Docker support will be added alongside the main stack (`docker-compose` service name `feedback-analytics-consumer`).

## Environment Variables
Set via `.env` or the Docker Compose stack:

| Variable | Default | Description |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker connection string. |
| `KAFKA_CONSUMER_GROUP` | `feedback-analytics-consumer` | Consumer group id override. |
| `KAFKA_SECURITY_PROTOCOL` | `PLAINTEXT` | Optional protocol for secured clusters. |

## Next Steps
- Generate the Spring Boot project skeleton (Day 2) following the structure in the notes.
- Implement logging/error-handling decisions and document outcomes in the notes/ADR.
