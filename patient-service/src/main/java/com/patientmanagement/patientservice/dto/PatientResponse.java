package com.patientmanagement.patientservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(name = "PatientResponse", description = "Patient details")
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
