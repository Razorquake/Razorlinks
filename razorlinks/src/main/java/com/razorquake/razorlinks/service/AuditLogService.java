package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.AuditLogFilter;
import com.razorquake.razorlinks.models.AuditLog;
import com.razorquake.razorlinks.models.ClickEvent;
import com.razorquake.razorlinks.models.UrlMapping;
import com.razorquake.razorlinks.repository.AuditLogRepository;
import com.razorquake.razorlinks.repository.specification.AuditLogSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private static final Set<String> AUDIT_SORT_FIELDS = Set.of(
            "timestamp",
            "action",
            "username",
            "shortUrl",
            "urlMappingId"
    );

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

    public Page<AuditLog> getAllAuditLogs(AuditLogFilter filter) {
        Specification<AuditLog> spec = AuditLogSpecification.buildSpecification(filter);
        Pageable pageable = PagingUtils.buildPageable(filter, "timestamp", AUDIT_SORT_FIELDS);
        return auditLogRepository.findAll(spec, pageable);
    }

    public Page<AuditLog> getAuditLogsByUrlId(Long id, AuditLogFilter filter) {
        filter.setUrlMappingId(id);
        return getAllAuditLogs(filter);
    }
}
