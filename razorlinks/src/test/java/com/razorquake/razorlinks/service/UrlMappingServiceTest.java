package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.*;
import com.razorquake.razorlinks.models.ClickEvent;
import com.razorquake.razorlinks.models.UrlMapping;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.ClickEventRepository;
import com.razorquake.razorlinks.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 🎭 WELCOME TO MOCKITO! 🎭
 *
 * THE PROBLEM:
 * UrlMappingService needs:
 * - UrlMappingRepository (talks to database)
 * - ClickEventRepository (talks to database)
 * - AuditLogService (another service)
 *
 * We DON'T want to:
 * ❌ Connect to a real database
 * ❌ Set up test data in database
 * ❌ Deal with database cleanup
 *
 * THE SOLUTION: MOCK IT! 🎭
 * We create FAKE versions of these dependencies that we control!
 */

@ExtendWith(MockitoExtension.class)
public class UrlMappingServiceTest {    // 🔑 This enables Mockito magic!

    /**
     * 🎭 @Mock creates a FAKE version of this class
     * It looks real, but it's empty - YOU decide what it returns!
     */
    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private ClickEventRepository clickEventRepository;

    @Mock
    private UrlRedirectLookupService urlRedirectLookupService;

    @Mock
    private AuditLogService auditLogService;

    /**
     * 🎯 @InjectMocks creates the REAL service we're testing
     * and INJECTS the mocks into it automatically!
     */
    @InjectMocks
    private UrlMappingService urlMappingService;

    // Test data we'll reuse
    private User testUser;
    private UrlMapping testUrlMapping;
    private UrlRedirectCache testUrlRedirectCache;

    @BeforeEach
    void setUp() {
        // Create test data
        testUser = new User();
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

        testUrlRedirectCache = new UrlRedirectCache(
                testUrlMapping.getId(),
                testUrlMapping.getShortUrl(),
                testUrlMapping.getOriginalUrl(),
                testUser.getUsername()
        );

        System.out.println("\n🎭 === NEW TEST STARTING ===");
    }

    /**
     * TEST 1: Creating a short URL
     * This is your FIRST real Mockito test!
     */
    @Test
    void createShortUrl_ValidInput_ReturnsUrlMappingDTO() {
        // ====== ARRANGE ======
        String originalUrl = "https://example.com";

        // 🎭 MOCK: Tell the repository what to return when save() is called
        // "When someone calls save() with ANY UrlMapping, return testUrlMapping"
        when(urlMappingRepository.save(any(UrlMapping.class)))
                .thenReturn(testUrlMapping);

        System.out.println("🎭 Mocked repository.save() to return: " + testUrlMapping.getShortUrl());

        // ====== ACT ======
        UrlMappingDTO result = urlMappingService.createShortUrl(originalUrl, testUser);

        // ====== ASSERT ======
        assertThat(result).isNotNull();
        assertThat(result.getOriginalUrl()).isEqualTo(originalUrl);
        assertThat(result.getShortUrl()).isNotBlank();
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());

        System.out.println("✅ Result: " + result.getShortUrl());

        // 🔍 VERIFY: Check that methods were actually called
        verify(urlMappingRepository, times(1)).save(any(UrlMapping.class));
        verify(auditLogService, times(1)).shortURLCreated(any(UrlMapping.class));

        System.out.println("✅ Verified repository.save() was called 1 time");
        System.out.println("✅ Verified auditLogService.shortURLCreated() was called 1 time");
    }

    /**
     * TEST 2: Getting original URL and incrementing click count
     * This test shows HOW POWERFUL mocking is!
     */
    @Test
    void getOriginalUrl_ValidShortLink_IncrementsClickCount() {
        // ====== ARRANGE ======
        String shortUrl = "abc12345";

        // 🎭 MOCK: Tell repository what to return
        when(clickEventRepository.save(any(ClickEvent.class)))
                .thenReturn(new ClickEvent());
        when(urlRedirectLookupService.resolve(shortUrl))
                .thenReturn(testUrlRedirectCache);
        when(urlMappingRepository.getReferenceById(testUrlMapping.getId()))
                        .thenReturn(testUrlMapping);



        System.out.println("🎭 Initial click count: " + testUrlMapping.getClickCount());

        // ====== ACT ======
        String result = urlMappingService.getOriginalUrl(shortUrl);

        // ====== ASSERT ======
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("https://example.com");

        // 🎯 IMPORTANT: Check the click count was incremented!
        // We use ArgumentCaptor to "capture" what was passed to save()
        verify(urlMappingRepository).incrementClickCount(testUrlMapping.getId());
        verify(urlMappingRepository).getReferenceById(testUrlMapping.getId());

        System.out.println("✅ Click count after redirect: " + testUrlMapping.getClickCount());

        // Verify a click event was created
        verify(clickEventRepository, times(1)).save(any(ClickEvent.class));
        System.out.println("✅ Click event was created");

        // Verify audit Log
        verify(auditLogService).shortURLClicked(
                eq(testUrlMapping.getId()),
                eq(testUrlMapping.getShortUrl()),
                eq(testUser.getUsername()),
                any(LocalDateTime.class)
        );
        System.out.println("✅ Audit log for click was created");
    }

    /**
     * TEST 3: Getting original URL with INVALID short link
     * Testing the SAD PATH (when things go wrong)
     */
    @Test
    void getOriginalUrl_InvalidShortLink_ReturnsNull() {
        // ====== ARRANGE ======
        String invalidShortUrl = "invalid123";

        // 🎭 MOCK: Tell repository to return null (URL not found)
        when(urlRedirectLookupService.resolve(invalidShortUrl))
                .thenReturn(null);

        System.out.println("🎭 Mocked repository to return null for: " + invalidShortUrl);

        // ====== ACT ======
        String result = urlMappingService.getOriginalUrl(invalidShortUrl);

        // ====== ASSERT ======
        assertThat(result).isNull();

        System.out.println("✅ Result is null (expected)");

        // 🔍 VERIFY: Save should NEVER be called if URL wasn't found
        verify(urlMappingRepository, never()).incrementClickCount(any(Long.class));
        verify(clickEventRepository, never()).save(any(ClickEvent.class));

        System.out.println("✅ Verified save() was NEVER called (correct!)");
    }

    /**
     * TEST 4: Getting URLs by user
     */
    @Test
    void getUrlsByUser_ReturnsPagedUserUrls() {
        // ====== ARRANGE ======
        UrlMappingFilter filter = new UrlMappingFilter();
        filter.setPage(1);
        filter.setSize(5);
        filter.setSortBy("shortUrl");
        filter.setSortOrder("ASC");

        Page<UrlMapping> urlMappings = new PageImpl<>(
                Collections.singletonList(testUrlMapping),
                PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "shortUrl")),
                1
        );
        when(urlMappingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(urlMappings);

        System.out.println("🎭 Mocked repository to return 1 URL page");

        // ====== ACT ======
        Page<UrlMappingDTO> result = urlMappingService.getUrlsByUser(testUser, filter);

        // ====== ASSERT ======
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getShortUrl()).isEqualTo("abc12345");
        assertThat(result.getContent().getFirst().getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(result.getNumber()).isEqualTo(1);

        System.out.println("✅ Got " + result.getContent().size() + " URLs for user: " + testUser.getUsername());

        verify(urlMappingRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * TEST 5: Deleting URL mapping - user OWNS the URL
     */
    @Test
    void deleteUrlMapping_UserOwnsUrl_DeletesSuccessfully() {
        // ====== ARRANGE ======
        String shortUrl = "abc12345";

        when(urlMappingRepository.findByShortUrl(shortUrl))
                .thenReturn(testUrlMapping);
        when(clickEventRepository.findByUrlMapping(testUrlMapping))
                .thenReturn(List.of());

        System.out.println("🎭 User " + testUser.getUsername() + " trying to delete " + shortUrl);

        // ====== ACT ======
        urlMappingService.deleteUrlMapping(shortUrl, testUser);

        // ====== ASSERT ======
        verify(urlMappingRepository, times(1)).delete(testUrlMapping);
        verify(auditLogService, times(1)).shortURLDeleted(testUrlMapping);

        System.out.println("✅ URL was deleted successfully");
        System.out.println("✅ Audit log was created");
    }

    /**
     * TEST 6: Deleting URL mapping - user DOES NOT own the URL
     * This is a SECURITY test! Very important!
     */
    @Test
    void deleteUrlMapping_UserDoesNotOwnUrl_DoesNotDelete() {
        // ====== ARRANGE ======
        String shortUrl = "abc12345";

        // Create a DIFFERENT user
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("hacker");

        when(urlMappingRepository.findByShortUrl(shortUrl))
                .thenReturn(testUrlMapping); // Belongs to testUser, not differentUser

        System.out.println("🎭 User 'hacker' trying to delete testuser's URL!");

        // ====== ACT ======
        urlMappingService.deleteUrlMapping(shortUrl, differentUser);

        // ====== ASSERT ======
        // 🔒 SECURITY: Delete should NEVER be called!
        verify(urlMappingRepository, never()).delete(any(UrlMapping.class));
        verify(auditLogService, never()).shortURLDeleted(any(UrlMapping.class));

        System.out.println("✅ Delete was BLOCKED (security works!)");
        System.out.println("✅ Audit log was NOT created (correct!)");
    }

    /**
     * TEST 7: Delete URL mapping - not found
     */
    @Test
    void deleteUrlMapping_UrlNotFound_DoesNothing() {
        // ====== ARRANGE ======
        when(urlMappingRepository.findByShortUrl("missing"))
                .thenReturn(null);

        // ====== ACT ======
        urlMappingService.deleteUrlMapping("missing", testUser);

        // ====== ASSERT ======
        verify(urlMappingRepository, never()).delete(any(UrlMapping.class));
        verify(clickEventRepository, never()).deleteAll(any());
        verify(auditLogService, never()).shortURLDeleted(any(UrlMapping.class));
    }

    /**
     * TEST 8: Get click events by date - URL missing
     */
    @Test
    void getClickEventByDate_UrlMissing_ReturnsNull() {
        // ====== ARRANGE ======
        when(urlMappingRepository.findByShortUrl("missing"))
                .thenReturn(null);
        ClickAnalyticsFilter filter = new ClickAnalyticsFilter();

        // ====== ACT ======
        Page<ClickEventDTO> result = urlMappingService.getClickEventByDate("missing", filter);

        // ====== ASSERT ======
        assertThat(result.getContent().isEmpty()).isTrue();
        verify(clickEventRepository, never()).findByUrlMappingAndClickDateBetween(any(), any(), any());
    }

    /**
     * TEST 9: Get click events by date - grouped results
     */
    @Test
    void getClickEventByDate_ValidUrl_GroupsByDate() {
        // ====== ARRANGE ======
        ClickAnalyticsFilter filter = new ClickAnalyticsFilter();
        filter.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        filter.setEndDate(LocalDateTime.of(2024, 1, 3, 0, 0));
        filter.setSortBy("clickDate");
        filter.setSortOrder("ASC");

        ClickEvent event1 = new ClickEvent();
        event1.setUrlMapping(testUrlMapping);
        event1.setClickDate(LocalDateTime.of(2024, 1, 1, 10, 0));

        ClickEvent event2 = new ClickEvent();
        event2.setUrlMapping(testUrlMapping);
        event2.setClickDate(LocalDateTime.of(2024, 1, 1, 12, 0));

        ClickEvent event3 = new ClickEvent();
        event3.setUrlMapping(testUrlMapping);
        event3.setClickDate(LocalDateTime.of(2024, 1, 2, 9, 30));

        when(urlMappingRepository.findByShortUrl(testUrlMapping.getShortUrl()))
                .thenReturn(testUrlMapping);
        when(clickEventRepository.findByUrlMappingAndClickDateBetween(
                eq(testUrlMapping),
                any(LocalDateTime.class),
                any(LocalDateTime.class))
        ).thenReturn(List.of(event1, event2, event3));

        // ====== ACT ======
        Page<ClickEventDTO> result = urlMappingService.getClickEventByDate(testUrlMapping.getShortUrl(), filter);

        // ====== ASSERT ======
        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(2);

        Map<LocalDate, Long> counts = result.getContent().stream()
                .collect(Collectors.toMap(ClickEventDTO::getClickDate, ClickEventDTO::getCount));

        assertThat(counts.get(LocalDate.of(2024, 1, 1))).isEqualTo(2L);
        assertThat(counts.get(LocalDate.of(2024, 1, 2))).isEqualTo(1L);
    }

    /**
     * TEST 10: Get total clicks by user and date range
     */
    @Test
    void getTotalClicksByUserAndDate_ReturnsPagedCounts() {
        // ====== ARRANGE ======
        ClickAnalyticsFilter filter = new ClickAnalyticsFilter();
        filter.setStartDate(LocalDateTime.of(2024, 1, 5, 0, 0));
        filter.setEndDate(LocalDateTime.of(2024, 1, 6, 23, 59, 59));

        ClickEvent event1 = new ClickEvent();
        event1.setUrlMapping(testUrlMapping);
        event1.setClickDate(LocalDateTime.of(2024, 1, 5, 10, 0));

        ClickEvent event2 = new ClickEvent();
        event2.setUrlMapping(testUrlMapping);
        event2.setClickDate(LocalDateTime.of(2024, 1, 5, 12, 0));

        ClickEvent event3 = new ClickEvent();
        event3.setUrlMapping(testUrlMapping);
        event3.setClickDate(LocalDateTime.of(2024, 1, 6, 9, 0));

        when(urlMappingRepository.findByUser(testUser)).thenReturn(List.of(testUrlMapping));
        when(clickEventRepository.findByUrlMappingInAndClickDateBetween(
                eq(List.of(testUrlMapping)),
                any(LocalDateTime.class),
                any(LocalDateTime.class))
        ).thenReturn(List.of(event1, event2, event3));

        // ====== ACT ======
        Page<ClickEventDTO> result = urlMappingService.getTotalClicksByUserAndDate(testUser, filter);

        // ====== ASSERT ======
        Map<LocalDate, Long> counts = result.getContent().stream()
                .collect(Collectors.toMap(ClickEventDTO::getClickDate, ClickEventDTO::getCount));

        assertThat(counts.get(LocalDate.of(2024, 1, 5))).isEqualTo(2L);
        assertThat(counts.get(LocalDate.of(2024, 1, 6))).isEqualTo(1L);
    }
}

/**
 * 🎓 WHAT YOU LEARNED:
 *
 * 1. @Mock - Create fake dependencies
 * 2. @InjectMocks - Inject mocks into the real service
 * 3. when().thenReturn() - Control what mocks return
 * 4. verify() - Check if methods were called
 * 5. times() - Check HOW MANY TIMES methods were called
 * 6. never() - Check methods were NOT called
 * 7. ArgumentCaptor - Capture and inspect arguments
 * 8. any() - Match any argument of a type
 *
 * This is REAL professional testing! 🎉
 */

