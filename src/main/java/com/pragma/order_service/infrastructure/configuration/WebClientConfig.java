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

}

