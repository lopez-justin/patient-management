package com.patientmanagement.patientservice.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponse(

        String id,
        String name,
        String email,
        String address,
        LocalDate birthDate,
        String phoneNumber,
        Instant createdAt,
        Instant updatedAt

) {
}
