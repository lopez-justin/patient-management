package com.patientmanagement.patientservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(max = 255)
        String address,

        @NotNull
        @PastOrPresent
        LocalDate birthDate,

        @NotBlank
        @Size(min = 7, max = 20)
        String phoneNumber

) {
}
