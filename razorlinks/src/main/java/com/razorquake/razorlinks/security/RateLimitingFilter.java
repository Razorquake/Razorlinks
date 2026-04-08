package com.razorquake.razorlinks.security;

import com.razorquake.razorlinks.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if the user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        // Admin users are exempt from rate limiting
        if (isAuthenticated && authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            filterChain.doFilter(request, response);
            return;
        }

        // Determine the bucket key and resolve the correct bucket
        String requestPath = request.getRequestURI();
        Bucket bucket;
        String bucketKey;

        if (requestPath.startsWith("/api/auth/public")) {
            // Public auth endpoints — rate limit by IP
            bucketKey = resolveClientIp(request);
            bucket = rateLimitConfig.resolveAuthBucket(bucketKey);
        } else if (requestPath.startsWith("/api/")) {
            // Authenticated API endpoints — rate limit by username (or IP as fallback)
            if (isAuthenticated) {
                bucketKey = authentication.getName();
                bucket = rateLimitConfig.resolveAuthenticatedBucket(bucketKey);
            } else {
                // Unauthenticated request to a protected endpoint — still apply IP-based limit
                // (Spring Security will reject it later with 401, but we prevent abuse)
                bucketKey = resolveClientIp(request);
                bucket = rateLimitConfig.resolveAuthBucket(bucketKey);
            }
        } else {
            // Everything else (redirect /{shortUrl}, etc.) — rate limit by IP
            bucketKey = resolveClientIp(request);
            bucket = rateLimitConfig.resolveRedirectBucket(bucketKey);
        }

        // Try to consume a token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request allowed — add rate limit headers and proceed
            response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Request denied — return 429
            long waitForRefillNanos = probe.getNanosToWaitForRefill();
            long retryAfterSeconds = Math.max(1, waitForRefillNanos / 1_000_000_000);

            log.warn("Rate limit exceeded for key '{}' on path '{}'", bucketKey, requestPath);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.addHeader("X-RateLimit-Remaining", "0");
            response.getWriter().write(
                    "{\"message\":\"Rate limit exceeded. Please try again later.\",\"status\":false}"
            );
        }
    }

    /**
     * Extracts the real client IP from the X-Forwarded-For header (for AWS CloudFront/ALB).
     * Falls back to request.getRemoteAddr() for direct connections (local dev).
     * <p>
     * X-Forwarded-For format: client, proxy1, proxy2
     * We take the first (leftmost) IP which is the original client.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP (original client)
            String clientIp = xForwardedFor.split(",")[0].trim();
            if (!clientIp.isEmpty()) {
                return clientIp;
            }
        }
        return request.getRemoteAddr();
    }
}
