package com.patientmanagement.billingservice.grpc;

import com.patientmanagement.billingservice.repository.BillingAccountRepository;
import com.patientmanagement.contracts.billing.v1.BillingAccountServiceGrpc;
import com.patientmanagement.contracts.billing.v1.BillingAccountStatus;
import com.patientmanagement.contracts.billing.v1.CreateBillingAccountRequest;
import com.patientmanagement.contracts.billing.v1.CreateBillingAccountResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestGrpcTransport
@ImportGrpcClients(
        target = "billing",
        types = BillingAccountServiceGrpc.BillingAccountServiceBlockingStub.class
)
class BillingAccountGrpcIntegrationTest {

    @Autowired
    private BillingAccountServiceGrpc.BillingAccountServiceBlockingStub stub;

    @Autowired
    private BillingAccountRepository repository;

    @BeforeEach
    void clearAccounts() {
        repository.deleteAll();
    }

    @Test
    void createsAccountOverGrpc() {
        CreateBillingAccountResponse response = stub.createBillingAccount(request("patient-id"));

        assertThat(response.getId()).isNotEmpty();
        assertThat(response.getPatientId()).isEqualTo("patient-id");
        assertThat(response.getStatus()).isEqualTo(BillingAccountStatus.BILLING_ACCOUNT_STATUS_ACTIVE);
        assertThat(response.getCreatedAt().getSeconds()).isPositive();
        assertThat(repository.findByPatientId("patient-id")).isPresent();
    }

    @Test
    void repeatedCallReturnsSameAccount() {
        CreateBillingAccountResponse first = stub.createBillingAccount(request("patient-id"));
        CreateBillingAccountResponse second = stub.createBillingAccount(request("patient-id"));

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void invalidRequestReturnsInvalidArgument() {
        CreateBillingAccountRequest request = request(" ");

        assertThatThrownBy(() -> stub.createBillingAccount(request))
                .isInstanceOfSatisfying(StatusRuntimeException.class, exception ->
                        assertThat(exception.getStatus().getCode())
                                .isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    private CreateBillingAccountRequest request(String patientId) {
        return CreateBillingAccountRequest.newBuilder()
                .setPatientId(patientId)
                .setPatientName("Justin Lopez")
                .setPatientEmail("a@example.com")
                .build();
    }
}
