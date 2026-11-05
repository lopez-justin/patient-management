package com.patientmanagement.patientservice.event;

import com.patientmanagement.contracts.patient.events.v1.PatientCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PatientEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PatientEventPublisher.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final String topic;

    public PatientEventPublisher(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            @Value("${app.kafka.topics.patient-created}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(PatientCreatedEvent event) {
        try {
            kafkaTemplate.send(topic, event.getPatientId(), event.toByteArray())
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.error(
                                    "Failed to publish patient-created event: eventId={}, patientId={}",
                                    event.getEventId(),
                                    event.getPatientId(),
                                    exception);
                        }
                    });
        } catch (RuntimeException exception) {
            log.error(
                    "Failed to publish patient-created event: eventId={}, patientId={}",
                    event.getEventId(),
                    event.getPatientId(),
                    exception);
        }
    }

}
