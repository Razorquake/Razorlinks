package com.razorquake.razorlinks.config;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class JteConfiguration {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public TemplateEngine templateEngine() {
        // For development - hot reload templates from src/main/jte
        if ("dev".equals(activeProfile) || "local".equals(activeProfile)) {
            Path templatePath = Path.of("src", "main", "jte");
            DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(templatePath);
            return TemplateEngine.create(codeResolver, ContentType.Html);
        }

        // For production - use precompiled templates from classpath
        return TemplateEngine.createPrecompiled(ContentType.Html);
    }

}