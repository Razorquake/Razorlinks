package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.AuditLogFilter;
import com.razorquake.razorlinks.models.AuditLog;
import com.razorquake.razorlinks.models.UrlMapping;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private User user;
    private UrlMapping urlMapping;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        urlMapping = new UrlMapping();
        urlMapping.setId(10L);
        urlMapping.setShortUrl("abc123");
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.of(2024, 1, 1, 0, 0));
    }

    @Test
    void shortURLCreated_CreatesAuditLog() {
        auditLogService.shortURLCreated(urlMapping);

        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());

        AuditLog log = logCaptor.getValue();
        assertThat(log.getAction()).isEqualTo("SHORT_URL_CREATED");
        assertThat(log.getUsername()).isEqualTo("testuser");
        assertThat(log.getUrlMappingId()).isEqualTo(10L);
        assertThat(log.getShortUrl()).isEqualTo("abc123");
        assertThat(log.getTimestamp()).isEqualTo(urlMapping.getCreatedDate());
    }

    @Test
    void shortURLDeleted_CreatesAuditLog() {
        auditLogService.shortURLDeleted(urlMapping);

        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());

        AuditLog log = logCaptor.getValue();
        assertThat(log.getAction()).isEqualTo("SHORT_URL_DELETED");
        assertThat(log.getUsername()).isEqualTo("testuser");
        assertThat(log.getUrlMappingId()).isEqualTo(10L);
        assertThat(log.getShortUrl()).isEqualTo("abc123");
        assertThat(log.getTimestamp()).isNotNull();
    }

    @Test
    void shortURLClicked_CreatesAuditLog() {
        auditLogService.shortURLClicked(
                urlMapping.getId(),
                urlMapping.getShortUrl(),
                user.getUsername(),
                LocalDateTime.of(2024, 1, 5, 12, 30)
        );

        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(logCaptor.capture());

        AuditLog log = logCaptor.getValue();
        assertThat(log.getAction()).isEqualTo("SHORT_URL_CLICKED");
        assertThat(log.getUsername()).isEqualTo("testuser");
        assertThat(log.getUrlMappingId()).isEqualTo(10L);
        assertThat(log.getShortUrl()).isEqualTo("abc123");
        assertThat(log.getTimestamp()).isEqualTo(LocalDateTime.of(2024, 1, 5, 12, 30));
    }

    @Test
    void getAllAuditLogs_ReturnsPagedRepositoryResults() {
        AuditLog log = new AuditLog();
        AuditLogFilter filter = new AuditLogFilter();
        Page<AuditLog> logs = new PageImpl<>(List.of(log), PageRequest.of(0, 10), 1);
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(logs);

        Page<AuditLog> result = auditLogService.getAllAuditLogs(filter);

        assertThat(result.getContent()).hasSize(1);
        verify(auditLogRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAuditLogsByUrlId_ReturnsPagedRepositoryResults() {
        AuditLog log = new AuditLog();
        AuditLogFilter filter = new AuditLogFilter();
        Page<AuditLog> logs = new PageImpl<>(List.of(log), PageRequest.of(0, 10), 1);
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(logs);

        Page<AuditLog> result = auditLogService.getAuditLogsByUrlId(10L, filter);

        assertThat(result.getContent()).hasSize(1);
        assertThat(filter.getUrlMappingId()).isEqualTo(10L);
        verify(auditLogRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
