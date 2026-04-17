package com.razorquake.razorlinks.repository.specification;

import com.razorquake.razorlinks.dtos.UserFilter;
import com.razorquake.razorlinks.models.AppRole;
import com.razorquake.razorlinks.models.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserSpecification {

    private static Specification<User> searchContains(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return null;
            }

            String normalized = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), normalized),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), normalized)
            );
        };
    }

    private static Specification<User> usernameContains(String username) {
        return (root, query, criteriaBuilder) ->
                username == null || username.isBlank() ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        "%" + username.toLowerCase() + "%"
                );
    }

    private static Specification<User> emailContains(String email) {
        return (root, query, criteriaBuilder) ->
                email == null || email.isBlank() ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"
                );
    }

    private static Specification<User> hasRole(AppRole roleName) {
        return (root, query, criteriaBuilder) ->
                roleName == null ? null : criteriaBuilder.equal(root.get("role").get("roleName"), roleName);
    }

    private static Specification<User> hasEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) ->
                enabled == null ? null : criteriaBuilder.equal(root.get("enabled"), enabled);
    }

    private static Specification<User> hasAccountNonLocked(Boolean accountNonLocked) {
        return (root, query, criteriaBuilder) ->
                accountNonLocked == null ? null : criteriaBuilder.equal(root.get("accountNonLocked"), accountNonLocked);
    }

    private static Specification<User> hasAccountNonExpired(Boolean accountNonExpired) {
        return (root, query, criteriaBuilder) ->
                accountNonExpired == null ? null : criteriaBuilder.equal(root.get("accountNonExpired"), accountNonExpired);
    }

    private static Specification<User> hasCredentialsNonExpired(Boolean credentialsNonExpired) {
        return (root, query, criteriaBuilder) ->
                credentialsNonExpired == null ? null : criteriaBuilder.equal(root.get("credentialsNonExpired"), credentialsNonExpired);
    }

    private static Specification<User> hasTwoFactorEnabled(Boolean twoFactorEnabled) {
        return (root, query, criteriaBuilder) ->
                twoFactorEnabled == null ? null : criteriaBuilder.equal(root.get("isTwoFactorEnabled"), twoFactorEnabled);
    }

    private static Specification<User> hasSignUpMethod(String signUpMethod) {
        return (root, query, criteriaBuilder) ->
                signUpMethod == null || signUpMethod.isBlank() ? null : criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("signUpMethod")),
                        signUpMethod.toLowerCase()
                );
    }

    private static Specification<User> createdBetween(LocalDate createdStartDate, LocalDate createdEndDate) {
        return (root, query, criteriaBuilder) -> {
            LocalDateTime start = createdStartDate == null ? null : createdStartDate.atStartOfDay();
            // Use the next day as an exclusive upper bound so an end date includes the full day.
            LocalDateTime endExclusive = createdEndDate == null ? null : createdEndDate.plusDays(1).atStartOfDay();

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

    private static Specification<User> matchAll() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    private static Specification<User> combine(
            Specification<User> current,
            Specification<User> next) {
        if (next == null) {
            return current;
        }
        return current == null ? next : current.and(next);
    }

    public static Specification<User> buildSpecification(UserFilter filter) {
        Specification<User> spec = matchAll();

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            spec = combine(spec, searchContains(filter.getSearch()));
        }

        if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
            spec = combine(spec, usernameContains(filter.getUsername()));
        }

        if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
            spec = combine(spec, emailContains(filter.getEmail()));
        }

        if (filter.getRoleName() != null) {
            spec = combine(spec, hasRole(filter.getRoleName()));
        }

        if (filter.getEnabled() != null) {
            spec = combine(spec, hasEnabled(filter.getEnabled()));
        }

        if (filter.getAccountNonLocked() != null) {
            spec = combine(spec, hasAccountNonLocked(filter.getAccountNonLocked()));
        }

        if (filter.getAccountNonExpired() != null) {
            spec = combine(spec, hasAccountNonExpired(filter.getAccountNonExpired()));
        }

        if (filter.getCredentialsNonExpired() != null) {
            spec = combine(spec, hasCredentialsNonExpired(filter.getCredentialsNonExpired()));
        }

        if (filter.getTwoFactorEnabled() != null) {
            spec = combine(spec, hasTwoFactorEnabled(filter.getTwoFactorEnabled()));
        }

        if (filter.getSignUpMethod() != null && !filter.getSignUpMethod().isBlank()) {
            spec = combine(spec, hasSignUpMethod(filter.getSignUpMethod()));
        }

        if (filter.getCreatedStartDate() != null || filter.getCreatedEndDate() != null) {
            spec = combine(spec, createdBetween(filter.getCreatedStartDate(), filter.getCreatedEndDate()));
        }

        return spec;
    }
}
