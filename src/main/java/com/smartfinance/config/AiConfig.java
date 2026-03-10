package com.smartfinance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Value("${app.ai.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Value("${app.ai.read-timeout-ms:15000}")
    private int readTimeoutMs;

    // Dedicated RestTemplate bean for Gemini AI calls with strict timeouts
    // to prevent blocking Tomcat threads when the AI endpoint is slow
    @Bean(name = "geminiRestTemplate")
    public RestTemplate geminiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
