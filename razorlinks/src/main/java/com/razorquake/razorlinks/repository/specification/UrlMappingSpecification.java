package com.razorquake.razorlinks.repository.specification;

import com.razorquake.razorlinks.dtos.UrlMappingFilter;
import com.razorquake.razorlinks.models.UrlMapping;
import com.razorquake.razorlinks.models.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UrlMappingSpecification {

    private static Specification<UrlMapping> belongsTo(User user) {
        return (root, query, criteriaBuilder) ->
                user == null ? null : criteriaBuilder.equal(root.get("user"), user);
    }

    private static Specification<UrlMapping> shortUrlContains(String shortUrl) {
        return (root, query, criteriaBuilder) ->
                shortUrl == null || shortUrl.isBlank() ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("shortUrl")),
                        "%" + shortUrl.toLowerCase() + "%"
                );
    }

    private static Specification<UrlMapping> originalUrlContains(String originalUrl) {
        return (root, query, criteriaBuilder) ->
                originalUrl == null || originalUrl.isBlank() ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("originalUrl")),
                        "%" + originalUrl.toLowerCase() + "%"
                );
    }

    private static Specification<UrlMapping> searchContains(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return null;
            }

            String normalized = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("shortUrl")), normalized),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("originalUrl")), normalized)
            );
        };
    }

    private static Specification<UrlMapping> clickCountBetween(Integer minClickCount, Integer maxClickCount) {
        return (root, query, criteriaBuilder) -> {
            if (minClickCount != null && maxClickCount != null) {
                return criteriaBuilder.between(root.get("clickCount"), minClickCount, maxClickCount);
            }
            if (minClickCount != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("clickCount"), minClickCount);
            }
            if (maxClickCount != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("clickCount"), maxClickCount);
            }
            return criteriaBuilder.conjunction();
        };
    }

    private static Specification<UrlMapping> createdBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            LocalDateTime start = startDate == null ? null : startDate.atStartOfDay();
            LocalDateTime endExclusive = endDate == null ? null : endDate.plusDays(1).atStartOfDay();

            if (start != null && endExclusive != null) {
                return criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), start),
                        criteriaBuilder.lessThan(root.get("createdDate"), endExclusive)
                );
            }
            if (start != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), start);
            }
            if (endExclusive != null) {
                return criteriaBuilder.lessThan(root.get("createdDate"), endExclusive);
            }
            return criteriaBuilder.conjunction();
        };
    }

    private static Specification<UrlMapping> matchAll() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    private static Specification<UrlMapping> combine(Specification<UrlMapping> current, Specification<UrlMapping> next) {
        if (next == null) {
            return current;
        }
        return current == null ? next : current.and(next);
    }

    public static Specification<UrlMapping> buildSpecification(User user, UrlMappingFilter filter) {
        Specification<UrlMapping> spec = matchAll();
        spec = combine(spec, belongsTo(user));

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            spec = combine(spec, searchContains(filter.getSearch()));
        }
        if (filter.getShortUrl() != null && !filter.getShortUrl().isBlank()) {
            spec = combine(spec, shortUrlContains(filter.getShortUrl()));
        }
        if (filter.getOriginalUrl() != null && !filter.getOriginalUrl().isBlank()) {
            spec = combine(spec, originalUrlContains(filter.getOriginalUrl()));
        }
        if (filter.getMinClickCount() != null || filter.getMaxClickCount() != null) {
            spec = combine(spec, clickCountBetween(filter.getMinClickCount(), filter.getMaxClickCount()));
        }
        if (filter.getCreatedStartDate() != null || filter.getCreatedEndDate() != null) {
            spec = combine(spec, createdBetween(filter.getCreatedStartDate(), filter.getCreatedEndDate()));
        }

        return spec;
    }
}
