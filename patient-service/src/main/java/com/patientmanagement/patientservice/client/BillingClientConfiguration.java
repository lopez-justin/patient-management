package com.patientmanagement.patientservice.client;

import com.patientmanagement.contracts.billing.v1.BillingAccountServiceGrpc;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;

// proxyBeanMethods = false is used to optimize the configuration class by preventing Spring from creating CGLIB proxies for the methods in this class.
// This can improve performance and reduce memory usage, especially when the configuration class does not need to be proxied.
@Configuration(proxyBeanMethods = false)
@ImportGrpcClients(
        target = "billing",
        types = BillingAccountServiceGrpc.BillingAccountServiceBlockingStub.class
) // This annotation imports the gRPC client for the BillingAccountServiceGrpc service, allowing it to be used in the Spring application context.
public class BillingClientConfiguration {
}
