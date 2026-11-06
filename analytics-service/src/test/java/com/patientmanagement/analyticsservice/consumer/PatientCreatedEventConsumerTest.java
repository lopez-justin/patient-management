package com.patientmanagement.analyticsservice.consumer;

import com.google.protobuf.Timestamp;
import com.patientmanagement.contracts.patient.events.v1.PatientCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class PatientCreatedEventConsumerTest {

    private final PatientCreatedEventConsumer consumer = new PatientCreatedEventConsumer();

    @Test
    void logsSafeEventFields(CapturedOutput output) throws Exception {
        PatientCreatedEvent event = PatientCreatedEvent.newBuilder()
                .setEventId("event-id")
                .setPatientId("patient-id")
                .setPatientName("Justin Lopez")
                .setPatientEmail("justinlopez@example.com")
                .setAddress("123 Main St")
                .setBirthDate("1990-01-15")
                .setPhoneNumber("123456789")
                .setOccurredAt(Timestamp.newBuilder().setSeconds(1234).build())
                .build();

        consumer.consume(new ConsumerRecord<>(
                "patient.created.v1",
                2,
                42L,
                "patient-id",
                event.toByteArray()));

        assertThat(output)
                .contains("eventId=event-id")
                .contains("patientId=patient-id")
                .contains("patientName=Justin Lopez")
                .contains("partition=2")
                .contains("offset=42")
                .doesNotContain("justinlopez@example.com")
                .doesNotContain("123 Main St")
                .doesNotContain("123456789");
    }

}
