package com.patientmanagement.analyticsservice.consumer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.patientmanagement.contracts.patient.events.v1.PatientCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PatientCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PatientCreatedEventConsumer.class);

    @KafkaListener(topics = "${app.kafka.topics.patient-created}")
    public void consume(ConsumerRecord<String, byte[]> record) throws InvalidProtocolBufferException {
        PatientCreatedEvent event = PatientCreatedEvent.parseFrom(record.value());
        log.info(
                "Patient created event received: eventId={}, patientId={}, patientName={}, partition={}, offset={}, occurredAt={}",
                event.getEventId(),
                event.getPatientId(),
                event.getPatientName(),
                record.partition(),
                record.offset(),
                Instant.ofEpochSecond(event.getOccurredAt().getSeconds(), event.getOccurredAt().getNanos()));
    }

}
