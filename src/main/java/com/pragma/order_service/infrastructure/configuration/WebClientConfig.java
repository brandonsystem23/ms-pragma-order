package com.pragma.order_service.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "usersWebClient")
    public WebClient usersWebClient(
            WebClient.Builder builder,
            @Value("${clients.users.base-url}") String usersBaseUrl
    ) {
        return builder
                .baseUrl(usersBaseUrl)
                .build();
    }

    @Bean(name = "notificationsWebClient")
    public WebClient notificationsWebClient(
            WebClient.Builder builder,
            @Value("${clients.notifications.base-url}") String notificationsBaseUrl
    ) {
        return builder
                .baseUrl(notificationsBaseUrl)
                .build();
    }

    @Bean(name = "traceabilityWebClient")
    public WebClient traceabilityWebClient(
            WebClient.Builder builder,
            @Value("${clients.traceability.base-url}") String traceabilityBaseUrl
    ) {
        return builder
                .baseUrl(traceabilityBaseUrl)
                .build();
    }
}
