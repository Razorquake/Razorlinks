package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.models.AuditLog;
import com.razorquake.razorlinks.models.ClickEvent;
import com.razorquake.razorlinks.models.UrlMapping;
import com.razorquake.razorlinks.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void shortURLCreated(UrlMapping urlMapping) {
        AuditLog log = new AuditLog();
        log.setAction("SHORT_URL_CREATED");
        log.setUsername(urlMapping.getUser().getUsername());
        log.setUrlMappingId(urlMapping.getId());
        log.setShortUrl(urlMapping.getShortUrl());
        log.setTimestamp(urlMapping.getCreatedDate());
        auditLogRepository.save(log);
    }

    public void shortURLDeleted(UrlMapping urlMapping) {
        AuditLog log = new AuditLog();
        log.setAction("SHORT_URL_DELETED");
        log.setUsername(urlMapping.getUser().getUsername());
        log.setUrlMappingId(urlMapping.getId());
        log.setShortUrl(urlMapping.getShortUrl());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public void shortURLClicked(ClickEvent clickEvent) {
        AuditLog log = new AuditLog();
        log.setAction("SHORT_URL_CLICKED");
        log.setUsername(clickEvent.getUrlMapping().getUser().getUsername());
        log.setUrlMappingId(clickEvent.getUrlMapping().getId());
        log.setShortUrl(clickEvent.getUrlMapping().getShortUrl());
        log.setTimestamp(clickEvent.getClickDate());
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getAuditLogsByUrlId(Long id) {
        return auditLogRepository.findByUrlMappingId(id);
    }
}
