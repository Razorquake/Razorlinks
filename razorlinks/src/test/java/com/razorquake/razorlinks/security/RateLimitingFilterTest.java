package com.razorquake.razorlinks.security;

import com.razorquake.razorlinks.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private RateLimitConfig rateLimitConfig;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    // --- Helper to build a real bucket with a given capacity ---
    private Bucket buildBucket(int capacity) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    // ========== OPTIONS bypass ==========

    @Test
    void optionsRequest_BypassesRateLimiting() throws Exception {
        request.setMethod("OPTIONS");
        request.setRequestURI("/api/auth/public/login");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // No bucket interaction should happen
        verifyNoInteractions(rateLimitConfig);
    }

    // ========== Admin exemption ==========

    @Test
    void adminUser_BypassesRateLimiting() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/urls/shorten");

        // Set up admin authentication
        var adminAuth = new UsernamePasswordAuthenticationToken(
                "admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimitConfig);
    }

    // ========== Auth endpoint (IP-based) ==========

    @Test
    void authEndpoint_Unauthenticated_UsesAuthBucketByIp() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/public/login");
        request.setRemoteAddr("192.168.1.100");

        Bucket bucket = buildBucket(10);
        when(rateLimitConfig.resolveAuthBucket("192.168.1.100")).thenReturn(bucket);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(rateLimitConfig).resolveAuthBucket("192.168.1.100");
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("9");
    }

    @Test
    void authEndpoint_WithXForwardedFor_UsesFirstIp() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/public/login");
        request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18, 150.172.238.178");

        Bucket bucket = buildBucket(5);
        when(rateLimitConfig.resolveAuthBucket("203.0.113.50")).thenReturn(bucket);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimitConfig).resolveAuthBucket("203.0.113.50");
        verify(filterChain).doFilter(request, response);
    }

    // ========== Authenticated API endpoint (username-based) ==========

    @Test
    void authenticatedApiEndpoint_UsesAuthenticatedBucketByUsername() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/urls/myurls");

        var userAuth = new UsernamePasswordAuthenticationToken(
                "testuser", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);

        Bucket bucket = buildBucket(30);
        when(rateLimitConfig.resolveAuthenticatedBucket("testuser")).thenReturn(bucket);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimitConfig).resolveAuthenticatedBucket("testuser");
        verify(filterChain).doFilter(request, response);
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("29");
    }

    @Test
    void unauthenticatedApiEndpoint_FallsBackToAuthBucketByIp() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/urls/myurls");
        request.setRemoteAddr("10.0.0.1");
        // No authentication set — anonymous user

        Bucket bucket = buildBucket(10);
        when(rateLimitConfig.resolveAuthBucket("10.0.0.1")).thenReturn(bucket);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimitConfig).resolveAuthBucket("10.0.0.1");
        verify(filterChain).doFilter(request, response);
    }

    // ========== Redirect endpoint (IP-based) ==========

    @Test
    void redirectEndpoint_UsesRedirectBucketByIp() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/abc123");
        request.setRemoteAddr("172.16.0.1");

        Bucket bucket = buildBucket(90);
        when(rateLimitConfig.resolveRedirectBucket("172.16.0.1")).thenReturn(bucket);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimitConfig).resolveRedirectBucket("172.16.0.1");
        verify(filterChain).doFilter(request, response);
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("89");
    }

    // ========== Rate limit exceeded (429) ==========

    @Test
    void rateLimitExceeded_Returns429WithRetryAfter() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/public/login");
        request.setRemoteAddr("1.2.3.4");

        // Create a bucket with 0 remaining tokens
        Bucket bucket = buildBucket(1);
        bucket.tryConsume(1); // exhaust the single token
        when(rateLimitConfig.resolveAuthBucket("1.2.3.4")).thenReturn(bucket);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Filter chain should NOT be called
        verify(filterChain, never()).doFilter(any(), any());

        // Should return 429
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getHeader("Retry-After")).isNotNull();
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(response.getContentAsString()).contains("Rate limit exceeded");
    }

    @Test
    void rateLimitExceeded_ResponseBodyHasCorrectFormat() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/public/register");
        request.setRemoteAddr("5.6.7.8");

        Bucket bucket = buildBucket(1);
        bucket.tryConsume(1);
        when(rateLimitConfig.resolveAuthBucket("5.6.7.8")).thenReturn(bucket);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        String body = response.getContentAsString();
        assertThat(body).contains("\"message\"");
        assertThat(body).contains("\"status\":false");
    }

    // ========== Remaining tokens count down ==========

    @Test
    void successiveRequests_DecrementRemainingTokens() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/shorturl");
        request.setRemoteAddr("9.9.9.9");

        Bucket bucket = buildBucket(3);
        when(rateLimitConfig.resolveRedirectBucket("9.9.9.9")).thenReturn(bucket);

        // Request 1
        rateLimitingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("2");

        // Request 2
        response = new MockHttpServletResponse();
        rateLimitingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("1");

        // Request 3
        response = new MockHttpServletResponse();
        rateLimitingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("0");

        // Request 4 — rate limited
        response = new MockHttpServletResponse();
        rateLimitingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getStatus()).isEqualTo(429);
    }

    // ========== Edge cases ==========

    @Test
    void xForwardedFor_EmptyHeader_FallsBackToRemoteAddr() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/link");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Forwarded-For", "");

        Bucket bucket = buildBucket(10);
        when(rateLimitConfig.resolveRedirectBucket("127.0.0.1")).thenReturn(bucket);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        verify(rateLimitConfig).resolveRedirectBucket("127.0.0.1");
    }

    @Test
    void adminUser_WithRoleUser_IsNotExempt() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/urls/myurls");

        // ROLE_USER, not ROLE_ADMIN
        var userAuth = new UsernamePasswordAuthenticationToken(
                "regularuser", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);

        Bucket bucket = buildBucket(30);
        when(rateLimitConfig.resolveAuthenticatedBucket("regularuser")).thenReturn(bucket);

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Should NOT bypass — should resolve a bucket
        verify(rateLimitConfig).resolveAuthenticatedBucket("regularuser");
    }
}
