package com.razorquake.razorlinks.repository;

import com.razorquake.razorlinks.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUrlMappingId(Long id);
}
