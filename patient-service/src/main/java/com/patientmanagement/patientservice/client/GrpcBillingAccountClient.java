package com.patientmanagement.patientservice.client;

import com.patientmanagement.contracts.billing.v1.BillingAccountServiceGrpc;
import com.patientmanagement.contracts.billing.v1.CreateBillingAccountRequest;
import com.patientmanagement.patientservice.exception.BillingServiceUnavailableException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class GrpcBillingAccountClient implements BillingAccountClient {

    private static final long DEADLINE_SECONDS = 10;

    private final BillingAccountServiceGrpc.BillingAccountServiceBlockingStub stub;

    public GrpcBillingAccountClient(
            BillingAccountServiceGrpc.BillingAccountServiceBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public void createBillingAccount(String patientId, String patientName, String patientEmail) {
        CreateBillingAccountRequest request = CreateBillingAccountRequest.newBuilder()
                .setPatientId(patientId)
                .setPatientName(patientName)
                .setPatientEmail(patientEmail)
                .build();

        try {
            stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .createBillingAccount(request);
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
                return;
            }
            throw new BillingServiceUnavailableException(exception);
        }
    }
}
