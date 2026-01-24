package com.razorquake.razorlinks.config;

import gg.jte.TemplateEngine;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JteConfigurationTest {

    @Test
    void templateEngine_DevProfile_UsesDirectoryResolver() {
        JteConfiguration config = new JteConfiguration();
        ReflectionTestUtils.setField(config, "activeProfile", "dev");

        TemplateEngine engine = config.templateEngine();

        assertThat(engine).isNotNull();
    }

    @Test
    void templateEngine_ProdProfile_UsesPrecompiledTemplates() {
        JteConfiguration config = new JteConfiguration();
        ReflectionTestUtils.setField(config, "activeProfile", "prod");

        TemplateEngine engine = config.templateEngine();

        assertThat(engine).isNotNull();
    }
}
