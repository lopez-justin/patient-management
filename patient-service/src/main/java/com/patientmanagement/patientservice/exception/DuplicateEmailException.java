package com.patientmanagement.patientservice.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A patient with email '" + email + "' already exists");
    }

}
