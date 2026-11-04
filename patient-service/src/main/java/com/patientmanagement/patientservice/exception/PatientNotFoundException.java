package com.patientmanagement.patientservice.exception;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(String id) {
        super("Patient not found with id: " + id);
    }

}
