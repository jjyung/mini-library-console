package com.example.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String ALL_PATHS = "/**";
    private static final String ALL_ORIGINS = "*";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(ALL_PATHS)
                .allowedOriginPatterns(ALL_ORIGINS)
                .allowedMethods(ALL_ORIGINS)
                .allowedHeaders(ALL_ORIGINS)
                .allowCredentials(false);
    }
}
