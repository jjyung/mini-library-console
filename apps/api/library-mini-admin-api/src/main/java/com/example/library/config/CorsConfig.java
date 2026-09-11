package com.example.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private static final String BOOKS_API_PATH = "/api/**";
    private static final String[] ALLOWED_METHODS = {"GET", "POST", "OPTIONS"};
    private static final String[] ALLOWED_HEADERS = {
            "Content-Type",
            "X-Correlation-Id",
            "Idempotency-Key"
    };
    private static final long PREFLIGHT_MAX_AGE_SECONDS = 3600L;

    private final String[] allowedOrigins;

    public CorsConfig(
            @Value("${library.cors.allowed-origins:http://localhost:5173,http://localhost:4173}")
            String allowedOrigins
    ) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(BOOKS_API_PATH)
                .allowedOrigins(allowedOrigins)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders(ALLOWED_HEADERS)
                .exposedHeaders("X-Correlation-Id")
                .allowCredentials(false)
                .maxAge(PREFLIGHT_MAX_AGE_SECONDS);
    }
}
