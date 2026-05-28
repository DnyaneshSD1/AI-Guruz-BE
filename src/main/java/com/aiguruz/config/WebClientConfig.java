package com.aiguruz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${anthropic.api-key:}") private String apiKey;

    @Bean
    public WebClient anthropicClient() {
        WebClient.Builder builder = WebClient.builder()
            .baseUrl("https://api.anthropic.com")
            .defaultHeader("anthropic-version",  "2023-06-01")
            .defaultHeader("Content-Type",        "application/json")
            .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024));

        if (!apiKey.isBlank()) {
            builder.defaultHeader("x-api-key", apiKey);
        }

        return builder.build();
    }
}

