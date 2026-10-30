package com.patientmanagement.patientservice.exception;

public class BillingServiceUnavailableException extends RuntimeException {

    public BillingServiceUnavailableException(Throwable cause) {
        super("Billing service is unavailable", cause);
    }

}
