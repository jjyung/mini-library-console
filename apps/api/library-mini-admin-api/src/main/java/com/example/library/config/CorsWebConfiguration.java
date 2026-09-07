package com.example.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Configuration
public class CorsWebConfiguration implements WebMvcConfigurer {

    private static final String ALL_PATHS = "/**";
    private static final String ALL_ORIGINS = "*";
    private static final String ALL_METHODS = "*";
    private static final String ALL_HEADERS = "*";
    private static final long PREFLIGHT_MAX_AGE_SECONDS = 3_600L;
    private static final Set<String> CORS_BYPASS_ENVIRONMENTS = Set.of("dev", "poc", "test");

    private final String environment;
    private final List<String> allowedOrigins;

    public CorsWebConfiguration(
            @Value("${app.environment:unknown}") String environment,
            @Value("${app.cors.allowed-origins:}") String configuredAllowedOrigins) {
        this.environment = normalizeEnvironment(environment);
        this.allowedOrigins = parseAllowedOrigins(configuredAllowedOrigins);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (isCorsBypassEnvironment()) {
            registry.addMapping(ALL_PATHS)
                    .allowedOriginPatterns(ALL_ORIGINS)
                    .allowedMethods(ALL_METHODS)
                    .allowedHeaders(ALL_HEADERS)
                    .allowCredentials(false)
                    .maxAge(PREFLIGHT_MAX_AGE_SECONDS);
            return;
        }

        if (!allowedOrigins.isEmpty()) {
            registry.addMapping(ALL_PATHS)
                    .allowedOrigins(allowedOrigins.toArray(String[]::new))
                    .allowedMethods(ALL_METHODS)
                    .allowedHeaders(ALL_HEADERS)
                    .allowCredentials(false)
                    .maxAge(PREFLIGHT_MAX_AGE_SECONDS);
        }
    }

    boolean isCorsBypassEnvironment() {
        return CORS_BYPASS_ENVIRONMENTS.contains(environment);
    }

    boolean hasExplicitAllowedOrigins() {
        return !allowedOrigins.isEmpty();
    }

    private static String normalizeEnvironment(String value) {
        return value == null ? "unknown" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static List<String> parseAllowedOrigins(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }
}
