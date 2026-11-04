package com.patientmanagement.billingservice.grpc;

import com.google.protobuf.Timestamp;
import com.patientmanagement.billingservice.entity.BillingAccountEntity;
import com.patientmanagement.billingservice.service.BillingAccountService;
import com.patientmanagement.contracts.billing.v1.BillingAccountServiceGrpc;
import com.patientmanagement.contracts.billing.v1.BillingAccountStatus;
import com.patientmanagement.contracts.billing.v1.CreateBillingAccountRequest;
import com.patientmanagement.contracts.billing.v1.CreateBillingAccountResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;

@GrpcService
public class BillingAccountGrpcService
        extends BillingAccountServiceGrpc.BillingAccountServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BillingAccountGrpcService.class);

    private final BillingAccountService billingAccountService;

    public BillingAccountGrpcService(BillingAccountService billingAccountService) {
        this.billingAccountService = billingAccountService;
    }

    @Override
    public void createBillingAccount(CreateBillingAccountRequest request,
                                     StreamObserver<CreateBillingAccountResponse> responseObserver) {

        log.info("Received request to create billing account: {}", request.toString());

        try {
            BillingAccountEntity account = billingAccountService.create(
                    request.getPatientId(),
                    request.getPatientName(),
                    request.getPatientEmail());

            responseObserver.onNext(CreateBillingAccountResponse.newBuilder()
                    .setId(account.getId())
                    .setPatientId(account.getPatientId())
                    .setStatus(toProto(account.getStatus()))
                    .setCreatedAt(toTimestamp(account.getCreatedAt()))
                    .build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(exception.getMessage())
                    .withCause(exception)
                    .asRuntimeException());
        } catch (DataIntegrityViolationException exception) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Billing account already exists for patient")
                    .withCause(exception)
                    .asRuntimeException());
        } catch (RuntimeException exception) {
            log.error("Unable to create billing account", exception);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unable to create billing account")
                    .withCause(exception)
                    .asRuntimeException());
        }
    }

    private BillingAccountStatus toProto(
            com.patientmanagement.billingservice.entity.BillingAccountStatus status) {
        return switch (status) {
            case ACTIVE -> BillingAccountStatus.BILLING_ACCOUNT_STATUS_ACTIVE;
        };
    }

    private Timestamp toTimestamp(Instant instant) {
        Instant value = instant != null ? instant : Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(value.getEpochSecond())
                .setNanos(value.getNano())
                .build();
    }
}
