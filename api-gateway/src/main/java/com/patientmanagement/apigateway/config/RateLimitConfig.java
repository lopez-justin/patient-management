package com.patientmanagement.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
public class RateLimitConfig {

    @Bean
    KeyResolver patientKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                .map(address -> address.getAddress() != null
                        ? address.getAddress().getHostAddress()
                        : address.getHostString())
                .defaultIfEmpty("unknown");
    }

}
