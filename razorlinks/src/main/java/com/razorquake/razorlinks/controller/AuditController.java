package com.razorquake.razorlinks.controller;

import com.razorquake.razorlinks.dtos.AuditLogFilter;
import com.razorquake.razorlinks.models.AuditLog;
import com.razorquake.razorlinks.service.AuditLogService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AuditController {
    private final AuditLogService auditLogService;

    @GetMapping
    public Page<AuditLog> getAllAuditLogs(@ModelAttribute @ParameterObject AuditLogFilter filter) {
        return auditLogService.getAllAuditLogs(filter);
    }

    @GetMapping("/urls/{id}")
    public Page<AuditLog> getAuditLogsByUrlId(@PathVariable Long id,
                                              @ModelAttribute @ParameterObject AuditLogFilter filter) {
        return auditLogService.getAuditLogsByUrlId(id, filter);
    }
}
