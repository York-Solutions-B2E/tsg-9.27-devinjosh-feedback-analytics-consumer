# Feedback Analytics Consumer

A Spring Boot Kafka consumer service that subscribes to the `feedback-submitted` topic and processes feedback events for analytics purposes. This service demonstrates event-driven architecture by consuming messages asynchronously from Kafka.

## Table of Contents

- [Overview](#overview)
- [Architecture & Role](#architecture--role)
- [Codebase Structure](#codebase-structure)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Event Contract](#event-contract)
- [Troubleshooting](#troubleshooting)

---

## Overview

The Feedback Analytics Consumer is a microservice that:

- **Subscribes** to the `feedback-submitted` Kafka topic
- **Deserializes** JSON feedback events into Java objects
- **Logs** structured analytics data for each event
- **Validates** event data (e.g., comment length limits)
- **Provides** health check endpoints for monitoring

---

## Architecture & Role

### How It Fits in the System

```markdown:tsg-9.27-devinjosh-feedback-analytics-consumer/README.md
┌─────────────┐
│ Feedback API│
│  (Producer) │
└──────┬──────┘
       │ Publishes to Kafka
       ▼
┌─────────────────────┐
│  Kafka Topic:       │
│ feedback-submitted  │
└──────┬──────────────┘
       │
       │ Consumes events
       ▼
┌─────────────────────┐
│ Analytics Consumer  │ ← You are here
│  (This Service)     │
└─────────────────────┘
       │
       ▼
   Logs analytics data
   (Structured logging)
```

### Data Flow

1. **Feedback API** receives feedback submission
2. **Feedback API** persists to database and publishes event to Kafka
3. **Analytics Consumer** (this service) automatically receives the event
4. **Analytics Consumer** logs structured analytics information
5. Logs can be collected by logging infrastructure (e.g., ELK, Splunk, CloudWatch)
---

## Codebase Structure

```
tsg-9.27-devinjosh-feedback-analytics-consumer/
├── src/
│   ├── main/
│   │   ├── java/com/tsg/feedbackconsumer/
│   │   │   ├── FeedbackAnalyticsConsumerApplication.java  # Main application class
│   │   │   ├── controllers/
│   │   │   │   └── HealthController.java                   # Health check endpoint
│   │   │   └── messaging/
│   │   │       ├── FeedbackEventListener.java              # Kafka listener
│   │   │       ├── FeedbackSubmittedEvent.java            # Event POJO (record)
│   │   │       └── KafkaConsumerConfig.java                # Kafka configuration
│   │   └── resources/
│   │       ├── application.properties                      # Default config
│   │       └── application-local.yml                      # Local profile config
│   └── test/
│       └── java/com/tsg/feedbackconsumer/
│           ├── FeedbackAnalyticsConsumerApplicationTests.java  # Context test
│           └── messaging/
│               └── FeedbackEventListenerTest.java         # Unit tests
├── Dockerfile                                              # Container image
├── pom.xml                                                 # Maven dependencies
├── mvnw / mvnw.cmd                                        # Maven wrapper
└── README.md                                               # This file
```

### Key Components

#### 1. `FeedbackEventListener.java`
- **Purpose**: Kafka message listener that processes events
- **Key Features**:
  - Subscribes to `feedback-submitted` topic
  - Logs structured analytics data
  - Validates comment length and logs warnings
- **Annotation**: `@KafkaListener` with custom container factory

#### 2. `KafkaConsumerConfig.java`
- **Purpose**: Configures Kafka consumer factory and listener container
- **Key Features**:
  - JSON deserialization with Jackson
  - Configures trusted packages for security
  - Sets acknowledgment mode to RECORD
  - Configures consumer group ID

#### 3. `FeedbackSubmittedEvent.java`
- **Purpose**: POJO (record) representing the event structure
- **Fields**:
  - `id` (String) - UUID of the feedback
  - `memberId` (String) - Member identifier
  - `providerName` (String) - Healthcare provider name
  - `rating` (int) - Rating 1-5
  - `comment` (String) - Optional feedback comment
  - `submittedAt` (Instant) - Timestamp
  - `schemaVersion` (int) - Event schema version

#### 4. `HealthController.java`
- **Purpose**: Provides health check endpoint
- **Endpoint**: `GET /health`
- **Response**: `{"status": "UP"}`

---

## Configuration

### Environment Variables

The service can be configured via environment variables or Spring profiles:

| Variable | Default | Description |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker connection string |
| `KAFKA_CONSUMER_GROUP` | `feedback-analytics-consumer` | Consumer group ID |
| `SPRING_PROFILES_ACTIVE` | `default` | Active Spring profile (`local`, `docker`) |

### Configuration Files

#### `application.properties` (Default)
- Basic Kafka connection settings
- Server port: `8081`
- Uses environment variable overrides

#### `application-local.yml` (Local Profile)
- More detailed configuration
- Explicit deserializer settings
- Health endpoint exposure
- Logging configuration

### Spring Profiles

- **`default`**: Uses `application.properties`
- **`local`**: Uses `application-local.yml` (recommended for local development)
- **`docker`**: Configured via Docker Compose environment variables

---

## Running the Application

### Option 1: Local Development

**Prerequisites**: Kafka must be running (use Docker Compose from parent project)

```bash
# Start Kafka infrastructure (from joshua-devin-final directory)
cd ../joshua-devin-final
docker compose up -d kafka

# Return to consumer directory
cd ../tsg-9.27-devinjosh-feedback-analytics-consumer

# Run with local profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Expected Output**:
```
Started FeedbackAnalyticsConsumerApplication in X.XXX seconds
```

### Option 2: Using Docker Compose (Full Stack)

From the `joshua-devin-final` directory:

```bash
# Start all services including this consumer
docker compose --profile app up -d analytics-consumer

# View logs
docker compose --profile app logs -f analytics-consumer
```

### Verifying the Service is Running

1. **Check Health Endpoint**:
   ```bash
   curl http://localhost:8081/health
   ```
   Expected: `{"status":"UP"}`

2. **Check Logs**:
   - Look for: `Subscribed to topic(s): feedback-submitted`
   - No connection errors

3. **Test Event Processing**:
   - Submit feedback via Feedback API
   - Check consumer logs for: `Received feedback event id=...`

---

## Testing

### Running All Tests

```bash
./mvnw clean test
```

**Windows PowerShell**:
```powershell
./mvnw clean test
```

**Expected Output**:
```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Test Breakdown

- **`FeedbackAnalyticsConsumerApplicationTests`**: 1 test
  - Verifies Spring application context loads successfully
  - Tests Kafka consumer configuration

- **`FeedbackEventListenerTest`**: 3 tests
  - `handleFeedbackSubmitted_logsInfo_forValidPayload`: Tests happy path logging
  - `handleFeedbackSubmitted_logsWarning_whenCommentTooLong`: Tests warning for long comments
  - `feedbackSubmittedEvent_roundTripsThroughConfiguredObjectMapper`: Tests JSON serialization/deserialization

### Running Specific Tests

```bash
# Run only event listener tests
./mvnw test -Dtest=FeedbackEventListenerTest

# Run only application context test
./mvnw test -Dtest=FeedbackAnalyticsConsumerApplicationTests
```

### Test Coverage

- **Event Listener Logic**: Logging, validation, event processing  
- **JSON Serialization**: Round-trip serialization/deserialization  
- **Application Context**: Spring Boot context loading  
- **Kafka Configuration**: Consumer factory and listener setup  

---

## Event Contract

### Topic

- **Name**: `feedback-submitted`
- **Key Type**: `String` (UUID of feedback)
- **Value Type**: `JSON` (FeedbackSubmittedEvent)

### Event Schema

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "memberId": "m-123",
  "providerName": "Dr. Smith",
  "rating": 4,
  "comment": "Great experience.",
  "submittedAt": "2025-11-19T16:23:00Z",
  "schemaVersion": 1
}
```
---

## Troubleshooting

### Issue: Consumer Not Receiving Messages

**Symptoms**: No log messages when feedback is submitted

**Solutions**:
1. **Verify Kafka is running**:
   ```bash
   docker compose ps kafka
   # Or check Kafka UI at http://localhost:8000
   ```

2. **Check consumer is subscribed**:
   - Look for log: `Subscribed to topic(s): feedback-submitted`
   - If missing, check Kafka connection string

3. **Verify topic exists**:
   ```bash
   docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
   ```
   Should include `feedback-submitted`

4. **Check consumer group**:
   ```bash
   docker exec -it kafka kafka-consumer-groups \
     --bootstrap-server localhost:9092 \
     --group feedback-analytics-consumer \
     --describe
   ```

### Issue: Connection Refused Errors

**Symptoms**: `Connection to node -1 (localhost/127.0.0.1:9092) could not be established`

**Solutions**:
1. **Start Kafka**:
   ```bash
   cd ../joshua-devin-final
   docker compose up -d kafka
   ```

2. **Verify Kafka is healthy**:
   ```bash
   docker compose ps kafka
   ```
   Should show status "healthy"

3. **Check port**: Ensure port 9092 is not blocked by firewall

### Issue: Deserialization Errors

**Symptoms**: `JsonMappingException` or `DeserializationException` in logs

**Solutions**:
1. **Verify event schema matches**: Check that Feedback API is publishing correct format
2. **Check trusted packages**: Ensure `com.tsg.feedbackconsumer.messaging` is in trusted packages
3. **Review event payload**: Use Kafka UI to inspect raw messages

### Issue: Tests Failing

**Symptoms**: Tests fail with Kafka connection errors

**Solutions**:
1. **Application context test** (`FeedbackAnalyticsConsumerApplicationTests`):
   - Kafka connection warnings are **expected** when Kafka isn't running
   - Test should still pass (it only tests context loading)

2. **Event listener tests** (`FeedbackEventListenerTest`):
   - These are unit tests and don't require Kafka
   - If failing, check test output for specific assertion errors

### Issue: No Logs Appearing

**Symptoms**: Service starts but no event logs

**Solutions**:
1. **Verify events are being published**: Check Feedback API logs or Kafka UI
2. **Check consumer offset**: Consumer may have already processed all messages
3. **Reset consumer group** (if needed):
   ```bash
   docker exec -it kafka kafka-consumer-groups \
     --bootstrap-server localhost:9092 \
     --group feedback-analytics-consumer \
     --reset-offsets \
     --to-earliest \
     --topic feedback-submitted \
     --execute
   ```


### Useful Links

- **Kafka UI**: http://localhost:8000 (when running via Docker Compose)
- **Health Endpoint**: http://localhost:8081/health