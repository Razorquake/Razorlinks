package com.razorquake.razorlinks.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigTest {

    private static final class TestCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> getConfigurations() {
            return super.getCorsConfigurations();
        }
    }

    @Test
    void addCorsMappings_RegistersFrontendOrigin() {
        WebConfig config = new WebConfig();
        ReflectionTestUtils.setField(config, "frontendUrl", "http://localhost:3000");

        TestCorsRegistry registry = new TestCorsRegistry();
        config.addCorsMappings(registry);

        Map<String, CorsConfiguration> configs = registry.getConfigurations();
        CorsConfiguration corsConfiguration = configs.get("/**");

        assertThat(corsConfiguration).isNotNull();
        assertThat(corsConfiguration.getAllowedOrigins()).contains("http://localhost:3000");
        assertThat(corsConfiguration.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(corsConfiguration.getAllowedHeaders()).contains("*");
        assertThat(corsConfiguration.getAllowCredentials()).isTrue();
        assertThat(corsConfiguration.getMaxAge()).isEqualTo(3600);
    }
}
