package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.UrlRedirectCache;
import com.razorquake.razorlinks.models.UrlMapping;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UrlRedirectLookupServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @InjectMocks
    private UrlRedirectLookupService urlRedirectLookupService;

    private UrlMapping testUrlMapping;

    @BeforeEach
    public void setUp() {
        // Create test data
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testUrlMapping = new UrlMapping();
        testUrlMapping.setId(1L);
        testUrlMapping.setOriginalUrl("https://example.com");
        testUrlMapping.setShortUrl("abc12345");
        testUrlMapping.setUser(testUser);
        testUrlMapping.setCreatedDate(LocalDateTime.now());
        testUrlMapping.setClickCount(0);
    }

    @Test
    public void resolveShortUrl_ValidShortUrl_ReturnsLongUrl() {
        // ====== ARRANGE ======
        String shortUrl = "abc12345";

        // 🎭 MOCK: Tell repository what to return
        when(urlMappingRepository.findByShortUrl(shortUrl))
                .thenReturn(testUrlMapping);

        // ====== ACT ======
        UrlRedirectCache result = urlRedirectLookupService.resolve(shortUrl);

        // ====== ASSERT ======
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.shortUrl()).isEqualTo(shortUrl);
        assertThat(result.originalUrl()).isEqualTo("https://example.com");
        assertThat(result.username()).isEqualTo("testuser");

        System.out.println("✅ Test passed: resolveShortUrl_ValidShortUrl_ReturnsLongUrl");
    }

    @Test
    public void resolveShortUrl_InvalidShortUrl_ReturnsNull() {
        // ====== ARRANGE ======
        String shortUrl = "invalid";

        // 🎭 MOCK: Tell repository to return null for invalid short URL
        when(urlMappingRepository.findByShortUrl(shortUrl))
                .thenReturn(null);

        // ====== ACT ======
        UrlRedirectCache result = urlRedirectLookupService.resolve(shortUrl);

        // ====== ASSERT ======
        assertThat(result).isNull();

        System.out.println("✅ Test passed: resolveShortUrl_InvalidShortUrl_ReturnsNull");
    }
}
