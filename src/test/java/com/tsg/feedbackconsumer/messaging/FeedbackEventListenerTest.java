// Run with .\mvnw clean verify (or add -Dtest=FeedbackEventListenerTest for focused execution).
package com.tsg.feedbackconsumer.messaging;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class FeedbackEventListenerTest {

    private final FeedbackEventListener listener = new FeedbackEventListener();

    /** Happy path test: Valid payload should log info message.
     * This test verifies that the listener logs the expected information when a valid feedback event is processed.
     * It captures the output and verifies that the expected log message is present.
     * @param output The captured output from the listener.
     * @throws Exception If the test fails.
     */
    @Test
    void handleFeedbackSubmitted_logsInfo_forValidPayload(CapturedOutput output) {
        FeedbackSubmittedEvent event = new FeedbackSubmittedEvent(
                "fb-123",
                "member-42",
                "York Clinic",
                5,
                "Great visit!",
                Instant.parse("2025-11-11T12:00:00Z"),
                1
        );

        listener.handleFeedbackSubmitted(event);

        assertThat(output.getAll())
                .contains("Received feedback event id=fb-123 memberId=member-42 provider=York Clinic rating=5 schemaVersion=1");
    }

    /** Test: Comment too long should log warning.
     * This test verifies that the listener logs a warning when a comment is too long.
     * It captures the output and verifies that the expected log message is present.
     * @param output The captured output from the listener.
     * @throws Exception If the test fails.
     */
    @Test
    void handleFeedbackSubmitted_logsWarning_whenCommentTooLong(CapturedOutput output) {
        FeedbackSubmittedEvent event = new FeedbackSubmittedEvent(
                "fb-456",
                "member-42",
                "York Clinic",
                4,
                "a".repeat(201),
                Instant.parse("2024-01-01T00:00:00Z"),
                1
        );

        listener.handleFeedbackSubmitted(event);

        assertThat(output.getAll())
                .contains("Received comment exceeding length limit for event id=fb-456");
    }

    /** Test: Feedback submitted event should round trip through configured object mapper.
     * This test verifies that the FeedbackSubmittedEvent can be serialized and deserialized using a configured ObjectMapper.
     * It creates an original event, serializes it to JSON, and then deserializes it back to an object.
     * It then verifies that the restored event is equal to the original event.
     * @throws Exception If the test fails.
    */
    @Test
    void feedbackSubmittedEvent_roundTripsThroughConfiguredObjectMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        FeedbackSubmittedEvent original = new FeedbackSubmittedEvent(
                "fb-789",
                "member-007",
                "York Clinic",
                3,
                "All good",
                Instant.parse("2024-02-02T12:34:56Z"),
                1
        );

        String json = mapper.writeValueAsString(original);
        FeedbackSubmittedEvent restored = mapper.readValue(json, FeedbackSubmittedEvent.class);

        assertThat(restored).isEqualTo(original);
    }
}

