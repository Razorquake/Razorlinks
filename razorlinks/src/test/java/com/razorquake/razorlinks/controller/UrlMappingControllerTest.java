package com.razorquake.razorlinks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.razorquake.razorlinks.dtos.ClickEventDTO;
import com.razorquake.razorlinks.dtos.UrlMappingDTO;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.security.jwt.JwtUtils;
import com.razorquake.razorlinks.service.QRCodeService;
import com.razorquake.razorlinks.service.UrlMappingService;
import com.razorquake.razorlinks.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🔗 Testing UrlMappingController
 *
 * This controller handles:
 * - Creating short URLs
 * - Getting user's URLs
 * - URL analytics
 * - Deleting URLs
 * - Generating QR codes
 */
@WebMvcTest(UrlMappingController.class)
@AutoConfigureMockMvc
@MockitoSettings(strictness = Strictness.LENIENT)
public class UrlMappingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlMappingService urlMappingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private QRCodeService qrCodeService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User testUser;
    private UrlMappingDTO testUrlMapping;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);

        testUrlMapping = new UrlMappingDTO();
        testUrlMapping.setId(1L);
        testUrlMapping.setOriginalUrl("https://example.com");
        testUrlMapping.setShortUrl("abc12345");
        testUrlMapping.setUsername("testuser");
        testUrlMapping.setClickCount(0);
        testUrlMapping.setCreateDate(LocalDateTime.now());

        System.out.println("\n🔗 === NEW URL CONTROLLER TEST ===");
    }

    /**
     * TEST 1: Create short URL - SUCCESS
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createShortUrl_ValidUrl_ReturnsShortUrl() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("originalUrl", "https://example.com");

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(urlMappingService.createShortUrl(anyString(), any(User.class)))
                .thenReturn(testUrlMapping);

        System.out.println("🎭 Mocked URL shortening");

        // Act & Assert
        mockMvc.perform(
                        post("/api/urls/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())  // Add CSRF token
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.shortUrl").value("abc12345"))
                .andExpect(jsonPath("$.username").value("testuser"));

        System.out.println("✅ Short URL created successfully");

        // Verify service was called
        verify(urlMappingService, times(1))
                .createShortUrl(eq("https://example.com"), eq(testUser));
    }

    /**
     * TEST 2: Create short URL - Unauthorized
     */
    @Test
    void createShortUrl_NotAuthenticated_Returns401() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("originalUrl", "https://example.com");

        System.out.println("🔒 Testing without authentication");

        // Act & Assert - No @WithMockUser, so should fail with 401 (Unauthorized)
        mockMvc.perform(
                        post("/api/urls/shorten")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf())  // Add CSRF token
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());  // 401 because user is not authenticated

        System.out.println("✅ Unauthorized access blocked");

        // Verify service was NEVER called
        verify(urlMappingService, never()).createShortUrl(anyString(), any());
    }

    /**
     * TEST 3: Get user's URLs
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyUrls_AuthenticatedUser_ReturnsUrlList() throws Exception {
        // Arrange
        List<UrlMappingDTO> userUrls = Collections.singletonList(testUrlMapping);

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(urlMappingService.getUrlsByUser(testUser)).thenReturn(userUrls);

        System.out.println("🎭 Mocked user URLs retrieval");

        // Act & Assert
        mockMvc.perform(
                        get("/api/urls/myurls")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].shortUrl").value("abc12345"))
                .andExpect(jsonPath("$[0].originalUrl").value("https://example.com"));

        System.out.println("✅ Retrieved user's URLs successfully");

        verify(urlMappingService, times(1)).getUrlsByUser(testUser);
    }

    /**
     * TEST 4: Get URL analytics
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getUrlAnalytics_ValidDateRange_ReturnsAnalytics() throws Exception {
        // Arrange
        String shortUrl = "abc12345";
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);

        ClickEventDTO event1 = new ClickEventDTO();
        event1.setClickDate(LocalDate.of(2024, 1, 15));
        event1.setCount(5L);

        ClickEventDTO event2 = new ClickEventDTO();
        event2.setClickDate(LocalDate.of(2024, 1, 16));
        event2.setCount(3L);

        List<ClickEventDTO> analytics = Arrays.asList(event1, event2);

        when(urlMappingService.getClickEventByDate(
                eq(shortUrl),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(analytics);

        System.out.println("🎭 Mocked analytics data");

        // Act & Assert
        mockMvc.perform(
                        get("/api/urls/analytics/{shortUrl}", shortUrl)
                                .param("startDate", start.toString())
                                .param("endDate", end.toString())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].count").value(5))
                .andExpect(jsonPath("$[1].count").value(3));

        System.out.println("✅ Analytics data retrieved successfully");
    }

    /**
     * TEST 5: Get total clicks by date
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getTotalClicks_ValidDateRange_ReturnsClicksByDate() throws Exception {
        // Arrange
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        Map<LocalDate, Long> clickData = new HashMap<>();
        clickData.put(LocalDate.of(2024, 1, 15), 10L);
        clickData.put(LocalDate.of(2024, 1, 16), 8L);

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(urlMappingService.getTotalClicksByUserAndDate(
                eq(testUser),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(clickData);

        System.out.println("🎭 Mocked total clicks data");

        // Act & Assert
        mockMvc.perform(
                        get("/api/urls/totalClicks")
                                .param("startDate", start.toString())
                                .param("endDate", end.toString())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['2024-01-15']").value(10))
                .andExpect(jsonPath("$['2024-01-16']").value(8));

        System.out.println("✅ Total clicks retrieved successfully");
    }

    /**
     * TEST 6: Delete URL mapping - SUCCESS
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteUrlMapping_UserOwnsUrl_DeletesSuccessfully() throws Exception {
        // Arrange
        String shortUrl = "abc12345";

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        doNothing().when(urlMappingService)
                .deleteUrlMapping(shortUrl, testUser);

        System.out.println("🎭 Mocked URL deletion");

        // Act & Assert
        mockMvc.perform(
                        delete("/api/urls/{shortUrl}", shortUrl)
                                .with(csrf())  // Add CSRF token
                )
                .andDo(print())
                .andExpect(status().isOk());

        System.out.println("✅ URL deleted successfully");

        // Verify deletion was called
        verify(urlMappingService, times(1))
                .deleteUrlMapping(shortUrl, testUser);
    }

    /**
     * TEST 7: Generate QR code - SUCCESS
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getQRCode_ValidShortUrl_ReturnsPNG() throws Exception {
        // Arrange
        String shortUrl = "abc12345";
        int size = 300;

        // Create fake PNG data (PNG signature)
        byte[] fakePngData = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47,  // PNG signature
                0x0D, 0x0A, 0x1A, 0x0A
        };

        when(qrCodeService.generateQRCode(shortUrl, size, size))
                .thenReturn(fakePngData);

        System.out.println("🎭 Mocked QR code generation");

        // Act & Assert
        mockMvc.perform(
                        get("/api/urls/qr/{shortUrl}", shortUrl)
                                .param("size", String.valueOf(size))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(fakePngData));

        System.out.println("✅ QR code generated successfully");

        verify(qrCodeService, times(1))
                .generateQRCode(shortUrl, size, size);
    }

    /**
     * TEST 8: Generate QR code - Default size
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getQRCode_NoSizeParam_UsesDefaultSize() throws Exception {
        // Arrange
        String shortUrl = "abc12345";
        byte[] fakePngData = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};

        when(qrCodeService.generateQRCode(eq(shortUrl), eq(300), eq(300)))
                .thenReturn(fakePngData);

        System.out.println("🎭 Testing default QR size");

        // Act & Assert
        mockMvc.perform(
                        get("/api/urls/qr/{shortUrl}", shortUrl)
                        // No size param - should use default 300
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));

        System.out.println("✅ Default size (300) used correctly");

        verify(qrCodeService, times(1))
                .generateQRCode(shortUrl, 300, 300);
    }

    /**
     * TEST 9: Generate QR code - Error handling
     */
    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getQRCode_ServiceThrowsException_Returns500() throws Exception {
        // Arrange
        String shortUrl = "abc12345";

        // Use WriterException which is caught by the controller
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt()))
                .thenThrow(new WriterException("QR generation failed"));

        System.out.println("🎭 Simulating QR generation error");

        // Act & Assert - The controller catches WriterException and returns 500
        mockMvc.perform(
                        get("/api/urls/qr/{shortUrl}", shortUrl)
                )
                .andDo(print())
                .andExpect(status().isInternalServerError());

        System.out.println("✅ Error handled correctly - returns 500");
    }
}

/**
 * 🎓 WHAT YOU LEARNED:
 *
 * 1. ✅ Testing authenticated endpoints with @WithMockUser
 * 2. ✅ Testing path variables (@PathVariable)
 * 3. ✅ Testing query parameters (@RequestParam)
 * 4. ✅ Testing binary data (PNG images)
 * 5. ✅ Testing DELETE requests
 * 6. ✅ Testing unauthorized access
 * 7. ✅ Verifying service method calls with verify()
 * 8. ✅ Testing error handling (exceptions)
 *
 * Great job! 🎉
 */
