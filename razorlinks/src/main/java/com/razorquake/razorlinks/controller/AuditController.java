package com.razorquake.razorlinks.controller;

import com.razorquake.razorlinks.models.AuditLog;
import com.razorquake.razorlinks.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AuditController {
    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLog> getAllAuditLogs() {
        return auditLogService.getAllAuditLogs();
    }

    @GetMapping("/urls/{id}")
    public List<AuditLog> getAuditLogsByUrlId(@PathVariable Long id) {
        return auditLogService.getAuditLogsByUrlId(id);
    }
}
