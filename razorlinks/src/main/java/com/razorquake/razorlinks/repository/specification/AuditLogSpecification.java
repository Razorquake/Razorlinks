package com.razorquake.razorlinks.repository.specification;

import com.razorquake.razorlinks.dtos.AuditLogFilter;
import com.razorquake.razorlinks.models.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AuditLogSpecification {

    private static Specification<AuditLog> actionContains(String action) {
        return (root, query, criteriaBuilder) ->
                action == null || action.isBlank() ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("action")),
                        "%" + action.toLowerCase() + "%"
                );
    }

    private static Specification<AuditLog> usernameContains(String username) {
        return (root, query, criteriaBuilder) ->
                username == null || username.isBlank() ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        "%" + username.toLowerCase() + "%"
                );
    }

    private static Specification<AuditLog> shortUrlContains(String shortUrl) {
        return (root, query, criteriaBuilder) ->
                shortUrl == null || shortUrl.isBlank() ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("shortUrl")),
                        "%" + shortUrl.toLowerCase() + "%"
                );
    }

    private static Specification<AuditLog> urlMappingIdEquals(Long urlMappingId) {
        return (root, query, criteriaBuilder) ->
                urlMappingId == null ? null : criteriaBuilder.equal(root.get("urlMappingId"), urlMappingId);
    }

    private static Specification<AuditLog> searchContains(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return null;
            }

            String normalized = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("action")), normalized),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), normalized),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("shortUrl")), normalized)
            );
        };
    }

    private static Specification<AuditLog> timestampBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            LocalDateTime start = startDate == null ? null : startDate.atStartOfDay();
            LocalDateTime endExclusive = endDate == null ? null : endDate.plusDays(1).atStartOfDay();

            if (start != null && endExclusive != null) {
                return criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), start),
                        criteriaBuilder.lessThan(root.get("timestamp"), endExclusive)
                );
            }
            if (start != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), start);
            }
            if (endExclusive != null) {
                return criteriaBuilder.lessThan(root.get("timestamp"), endExclusive);
            }
            return criteriaBuilder.conjunction();
        };
    }

    private static Specification<AuditLog> matchAll() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    private static Specification<AuditLog> combine(Specification<AuditLog> current, Specification<AuditLog> next) {
        if (next == null) {
            return current;
        }
        return current == null ? next : current.and(next);
    }

    public static Specification<AuditLog> buildSpecification(AuditLogFilter filter) {
        Specification<AuditLog> spec = matchAll();

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            spec = combine(spec, searchContains(filter.getSearch()));
        }
        if (filter.getAction() != null && !filter.getAction().isBlank()) {
            spec = combine(spec, actionContains(filter.getAction()));
        }
        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            spec = combine(spec, usernameContains(filter.getUsername()));
        }
        if (filter.getShortUrl() != null && !filter.getShortUrl().isBlank()) {
            spec = combine(spec, shortUrlContains(filter.getShortUrl()));
        }
        if (filter.getUrlMappingId() != null) {
            spec = combine(spec, urlMappingIdEquals(filter.getUrlMappingId()));
        }
        if (filter.getStartDate() != null || filter.getEndDate() != null) {
            spec = combine(spec, timestampBetween(filter.getStartDate(), filter.getEndDate()));
        }

        return spec;
    }
}
