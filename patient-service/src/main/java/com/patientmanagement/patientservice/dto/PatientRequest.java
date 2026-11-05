package com.patientmanagement.patientservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(name = "PatientRequest", description = "Patient creation or update payload")
public record PatientRequest(

        @Schema(description = "Full name", example = "Jane Doe")
        @NotBlank
        @Size(max = 150)
        String name,

        @Schema(description = "Unique email address", example = "jane.doe@example.com")
        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @Schema(description = "Street address", example = "123 Main St")
        @NotBlank
        @Size(max = 255)
        String address,

        @Schema(description = "Birth date", example = "1990-01-15")
        @NotNull
        @PastOrPresent
        LocalDate birthDate,

        @Schema(description = "Phone number", example = "123456789")
        @NotBlank
        @Size(min = 7, max = 20)
        String phoneNumber

) {
}
