package com.patientmanagement.patientservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI patientServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Patient Service API")
                        .description("REST API for managing patients.")
                        .version("v1"));
    }

}
