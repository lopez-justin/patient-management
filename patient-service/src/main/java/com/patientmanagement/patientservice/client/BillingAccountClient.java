package com.patientmanagement.patientservice.client;

public interface BillingAccountClient {

    void createBillingAccount(String patientId, String patientName, String patientEmail);

}
