package com.razorquake.razorlinks.controller;

import com.razorquake.razorlinks.models.AuditLog;
import com.razorquake.razorlinks.security.jwt.JwtUtils;
import com.razorquake.razorlinks.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
@AutoConfigureMockMvc
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(AuditControllerTest.TestSecurityConfig.class)
class AuditControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private com.razorquake.razorlinks.config.RateLimitConfig rateLimitConfig;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllAuditLogs_AdminRole_ReturnsLogs() throws Exception {
        AuditLog log = new AuditLog();
        log.setAction("SHORT_URL_CREATED");
        log.setUsername("testuser");
        log.setUrlMappingId(1L);
        log.setShortUrl("abc123");
        log.setTimestamp(LocalDateTime.of(2024, 1, 1, 0, 0));

        when(auditLogService.getAllAuditLogs()).thenReturn(List.of(log));

        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("SHORT_URL_CREATED"))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].urlMappingId").value(1));

        verify(auditLogService).getAllAuditLogs();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAuditLogsByUrlId_AdminRole_ReturnsLogs() throws Exception {
        AuditLog log = new AuditLog();
        log.setAction("SHORT_URL_CLICKED");
        log.setUsername("testuser");
        log.setUrlMappingId(10L);
        log.setShortUrl("abc123");
        log.setTimestamp(LocalDateTime.of(2024, 1, 2, 0, 0));

        when(auditLogService.getAuditLogsByUrlId(10L)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/audit/urls/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("SHORT_URL_CLICKED"))
                .andExpect(jsonPath("$[0].urlMappingId").value(10));

        verify(auditLogService).getAuditLogsByUrlId(10L);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getAllAuditLogs_UserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isForbidden());

        verify(auditLogService, never()).getAllAuditLogs();
    }

    @Test
    void getAllAuditLogs_NotAuthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isUnauthorized());
    }

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/audit/**").authenticated()
                            .anyRequest().permitAll()
                    )
                    .exceptionHandling(exceptionHandling ->
                            exceptionHandling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                    );
            return http.build();
        }
    }
}
