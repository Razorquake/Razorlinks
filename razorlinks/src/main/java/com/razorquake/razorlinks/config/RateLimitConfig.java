package com.razorquake.razorlinks.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Getter
public class RateLimitConfig {

    // --- Auth endpoints (per IP) ---
    @Value("${rate-limit.auth.capacity:10}")
    private int authCapacity;

    @Value("${rate-limit.auth.refill-tokens:10}")
    private int authRefillTokens;

    @Value("${rate-limit.auth.refill-duration-seconds:60}")
    private int authRefillDurationSeconds;

    // --- Redirect endpoints (per IP) ---
    @Value("${rate-limit.redirect.capacity:90}")
    private int redirectCapacity;

    @Value("${rate-limit.redirect.refill-tokens:90}")
    private int redirectRefillTokens;

    @Value("${rate-limit.redirect.refill-duration-seconds:60}")
    private int redirectRefillDurationSeconds;

    // --- Authenticated API endpoints (per username) ---
    @Value("${rate-limit.authenticated.capacity:30}")
    private int authenticatedCapacity;

    @Value("${rate-limit.authenticated.refill-tokens:30}")
    private int authenticatedRefillTokens;

    @Value("${rate-limit.authenticated.refill-duration-seconds:60}")
    private int authenticatedRefillDurationSeconds;

    // Separate maps for each tier to avoid key collisions
    // (e.g., IP "10.0.0.1" and username "10.0.0.1" won't clash)
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> redirectBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authenticatedBuckets = new ConcurrentHashMap<>();

    /**
     * Resolves (or creates) a rate limit bucket for auth endpoints, keyed by IP.
     */
    public Bucket resolveAuthBucket(String ip) {
        return authBuckets.computeIfAbsent(ip, k -> buildBucket(authCapacity, authRefillTokens, authRefillDurationSeconds));
    }

    /**
     * Resolves (or creates) a rate limit bucket for redirect endpoints, keyed by IP.
     */
    public Bucket resolveRedirectBucket(String ip) {
        return redirectBuckets.computeIfAbsent(ip, k -> buildBucket(redirectCapacity, redirectRefillTokens, redirectRefillDurationSeconds));
    }

    /**
     * Resolves (or creates) a rate limit bucket for authenticated API endpoints, keyed by username.
     */
    public Bucket resolveAuthenticatedBucket(String username) {
        return authenticatedBuckets.computeIfAbsent(username, k -> buildBucket(authenticatedCapacity, authenticatedRefillTokens, authenticatedRefillDurationSeconds));
    }

    private Bucket buildBucket(int capacity, int refillTokens, int refillDurationSeconds) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, Duration.ofSeconds(refillDurationSeconds))
                .build();
        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}
