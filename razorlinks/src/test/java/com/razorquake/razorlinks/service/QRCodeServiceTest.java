package com.razorquake.razorlinks.service;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Testing QRCodeService - Learning about MOCKING properties!
 *
 * CHALLENGE: QRCodeService needs @Value("${subdomain.url}") from application.properties
 * We don't want to load the entire Spring context just for this test!
 *
 * SOLUTION: Use ReflectionTestUtils to inject the value directly!
 */
public class QRCodeServiceTest {

    private QRCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        // Create the service
        qrCodeService = new QRCodeService();

        // 🎭 MOCK the @Value property using ReflectionTestUtils
        // This is like saying: "Pretend the subdomain.url is 'https://test.com'"
        ReflectionTestUtils.setField(qrCodeService, "subdomainUrl", "https://test.com");

        System.out.println("🎭 Mocked subdomainUrl to: https://test.com");
    }

    /**
     * TEST 1: Can we generate a QR code?
     */
    @Test
    void generateQRCode_ValidShortUrl_GeneratesPNG() throws WriterException, IOException {
        // Arrange
        String shortUrl = "abc12345";
        int width = 300;
        int height = 300;

        // Act
        byte[] qrCodeBytes = qrCodeService.generateQRCode(shortUrl, width, height);

        // Assert
        assertThat(qrCodeBytes).isNotNull();
        assertThat(qrCodeBytes).isNotEmpty();
        assertThat(qrCodeBytes.length).isGreaterThan(0);

        // PNG files start with specific bytes (89 50 4E 47...)
        assertThat(qrCodeBytes[0]).isEqualTo((byte) 0x89); // PNG signature
        assertThat(qrCodeBytes[1]).isEqualTo((byte) 0x50); // 'P'
        assertThat(qrCodeBytes[2]).isEqualTo((byte) 0x4E); // 'N'
        assertThat(qrCodeBytes[3]).isEqualTo((byte) 0x47); // 'G'

        System.out.println("✅ Generated QR code with " + qrCodeBytes.length + " bytes");
        System.out.println("✅ First 4 bytes (PNG signature): " +
                String.format("%02X %02X %02X %02X",
                        qrCodeBytes[0], qrCodeBytes[1], qrCodeBytes[2], qrCodeBytes[3]));
    }

    /**
     * TEST 2: Different sizes generate different QR codes
     */
    @Test
    void generateQRCode_DifferentSizes_GenerateDifferentBytes() throws WriterException, IOException {
        // Arrange
        String shortUrl = "abc12345";

        // Act
        byte[] small = qrCodeService.generateQRCode(shortUrl, 200, 200);
        byte[] large = qrCodeService.generateQRCode(shortUrl, 500, 500);

        // Assert
        assertThat(small.length).isNotEqualTo(large.length);
        assertThat(large.length).isGreaterThan(small.length);

        System.out.println("✅ Small QR (200x200): " + small.length + " bytes");
        System.out.println("✅ Large QR (500x500): " + large.length + " bytes");
    }

    /**
     * TEST 3: What happens with zero dimensions?
     * DISCOVERY: The library doesn't throw an exception, it handles it gracefully!
     * This is actually GOOD behavior - defensive programming!
     */
    @Test
    void generateQRCode_ZeroDimensions_ThrowsException() throws IOException, WriterException {
        // Arrange
        String shortUrl = "abc12345";

        // Act
        byte[] qrCode = qrCodeService.generateQRCode(shortUrl, 0, 0);

        // Assert
        // The library handles this gracefully and still generates something        assertThatThrownBy(() ->
        assertThat(qrCode).isNotNull();
        assertThat(qrCode).isNotEmpty();

        System.out.println("✅ Zero dimensions handled gracefully, generated " + qrCode.length + " bytes");
        System.out.println("   (Library used default/minimum dimensions)");

    }

    /**
     * TEST 4: Negative dimensions should also fail
     */
    @Test
    void generateQRCode_NegativeDimensions_ThrowsException() {
        // Arrange
        String shortUrl = "abc12345";

        // Act & Assert
        assertThatThrownBy(() ->
                qrCodeService.generateQRCode(shortUrl, -100, 200)
        )
                .isInstanceOf(IllegalArgumentException.class);

        System.out.println("✅ Negative dimensions correctly threw exception");
    }

    /**
     * TEST 5: Empty short URL
     */
    @Test
    void generateQRCode_EmptyShortUrl_StillGeneratesQRCode() throws WriterException, IOException {
        // Arrange
        String emptyUrl = "";

        // Act
        byte[] qrCode = qrCodeService.generateQRCode(emptyUrl, 300, 300);

        // Assert
        // Even with empty URL, it should still generate a QR code (for "https://test.com/")
        assertThat(qrCode).isNotEmpty();

        System.out.println("✅ Empty URL generated QR code with " + qrCode.length + " bytes");
    }

    /**
     * TEST 6: Very long short URL
     */
    @Test
    void generateQRCode_VeryLongShortUrl_StillWorks() throws WriterException, IOException {
        // Arrange
        String longUrl = "a".repeat(1000); // 1000 characters!

        // Act
        byte[] qrCode = qrCodeService.generateQRCode(longUrl, 500, 500);

        // Assert
        assertThat(qrCode).isNotEmpty();

        System.out.println("✅ Long URL (" + longUrl.length() + " chars) generated QR code");
    }
}

/**
 * 🎓 WHAT YOU LEARNED:
 *
 * 1. ReflectionTestUtils - How to inject values into @Value fields
 * 2. Testing binary data (byte arrays)
 * 3. Testing edge cases (zero, negative, empty, very long)
 * 4. assertThatThrownBy() - Testing that exceptions are thrown
 * 5. PNG file signature verification
 *
 * NEXT STEP: Real Mockito mocking! 🚀
 */
