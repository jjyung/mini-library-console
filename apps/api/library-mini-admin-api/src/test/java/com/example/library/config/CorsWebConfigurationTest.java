package com.example.library.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorsWebConfigurationTest {

    @Test
    void lowerEnvironmentConfigurationUsesTheBypassMapping() {
        CorsWebConfiguration configuration = new CorsWebConfiguration("PoC", "");

        assertThat(configuration.isCorsBypassEnvironment()).isTrue();
    }

    @Test
    void unknownEnvironmentDoesNotFallBackToPermissiveCors() {
        CorsWebConfiguration configuration = new CorsWebConfiguration("unknown", "");

        assertThat(configuration.isCorsBypassEnvironment()).isFalse();
        assertThat(configuration.hasExplicitAllowedOrigins()).isFalse();
    }
}
