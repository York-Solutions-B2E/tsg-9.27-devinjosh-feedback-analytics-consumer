/*
 * This class is a POJO that represents the feedback submitted event.
 * It mirrors the event payload so spring can deserialize the event into this object.
 */
package com.tsg.feedbackconsumer.messaging;

import java.time.Instant;

public record FeedbackSubmittedEvent(
    String id,
    String memberId,
    String providerName,
    int rating,
    String comment,
    Instant submittedAt,
    int schemaVersion
) {
    
}
