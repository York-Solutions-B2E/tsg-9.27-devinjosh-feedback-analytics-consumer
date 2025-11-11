package com.tsg.feedbackconsumer.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FeedbackEventListener {

    private static final Logger log = LoggerFactory.getLogger(FeedbackEventListener.class);

    @KafkaListener(
            topics = "feedback-submitted",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleFeedbackSubmitted(FeedbackSubmittedEvent event) {
        log.info(
                "Received feedback event id={} memberId={} provider={} rating={} schemaVersion={}",
                event.id(),
                event.memberId(),
                event.providerName(),
                event.rating(),
                event.schemaVersion()
        );

        if (event.comment() != null && event.comment().length() > 200) {
            log.warn("Received comment exceeding length limit for event id={}", event.id());
        }
    }
}

