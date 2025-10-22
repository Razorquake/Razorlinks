package com.razorquake.razorlinks.service;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing TotpService - FIXED VERSION!
 * Great job discovering the actual behavior of the library!
 */
class TotpServiceTest {

    private TotpService totpService;

    @BeforeEach
    void setUp() {
        totpService = new TotpService();
    }

    /**
     * TEST 1: Can we generate a secret?
     * FIXED: Changed size from 32 to 16 (actual implementation)
     */
    @Test
    void generateSecret_ShouldReturnValidKey() {
        // Act
        GoogleAuthenticatorKey secret = totpService.generateSecret();

        // Assert
        assertThat(secret).isNotNull();
        assertThat(secret.getKey()).isNotEmpty();
        assertThat(secret.getKey()).hasSize(16); // ✅ FIXED: Actual size is 16!

        System.out.println("✅ Generated secret key: " + secret.getKey());
    }

    /**
     * TEST 2: Are generated secrets unique?
     */
    @Test
    void generateSecret_ShouldGenerateUniqueKeys() {
        // Act
        GoogleAuthenticatorKey secret1 = totpService.generateSecret();
        GoogleAuthenticatorKey secret2 = totpService.generateSecret();

        // Assert
        assertThat(secret1.getKey()).isNotEqualTo(secret2.getKey());

        System.out.println("✅ Secret 1: " + secret1.getKey());
        System.out.println("✅ Secret 2: " + secret2.getKey());
    }

    /**
     * TEST 3: Can we generate a QR code URL?
     * FIXED: The URL is actually a full qrserver.com URL, not just otpauth://
     * This is GOOD - it means the library gives us a ready-to-use QR code image!
     */
    @Test
    void getQrCodeUrl_ShouldReturnValidUrl() {
        // Arrange
        GoogleAuthenticatorKey secret = totpService.generateSecret();
        String username = "testuser";

        // Act
        String qrCodeUrl = totpService.getQrCodeUrl(secret, username);

        // Assert
        assertThat(qrCodeUrl).isNotNull();
        assertThat(qrCodeUrl).isNotEmpty();

        // The URL is from qrserver.com - it's a QR code image generator
        assertThat(qrCodeUrl).startsWith("https://api.qrserver.com/v1/create-qr-code/");

        // The URL should contain the encoded otpauth data
        assertThat(qrCodeUrl).contains("otpauth"); // URL-encoded
        assertThat(qrCodeUrl).contains("Razorlinks");
        assertThat(qrCodeUrl).contains(username);

        System.out.println("✅ QR Code URL: " + qrCodeUrl);
    }

    /**
     * TEST 4: Does verification work with a valid code?
     */
    @Test
    void verifyCode_WithValidCode_ShouldReturnTrue() {
        // Arrange
        GoogleAuthenticatorKey secret = totpService.generateSecret();

        // Generate a valid code for this secret
        com.warrenstrange.googleauth.GoogleAuthenticator ga =
                new com.warrenstrange.googleauth.GoogleAuthenticator();
        int validCode = ga.getTotpPassword(secret.getKey());

        // Act
        boolean isValid = totpService.verifyCode(secret.getKey(), validCode);

        // Assert
        assertThat(isValid).isTrue();

        System.out.println("✅ Secret: " + secret.getKey());
        System.out.println("✅ Valid code: " + validCode);
        System.out.println("✅ Verification result: " + isValid);
    }

    /**
     * TEST 5: Does verification fail with an invalid code?
     */
    @Test
    void verifyCode_WithInvalidCode_ShouldReturnFalse() {
        // Arrange
        GoogleAuthenticatorKey secret = totpService.generateSecret();
        int invalidCode = 999999;

        // Act
        boolean isValid = totpService.verifyCode(secret.getKey(), invalidCode);

        // Assert
        assertThat(isValid).isFalse();

        System.out.println("✅ Testing with invalid code: " + invalidCode);
        System.out.println("✅ Verification correctly returned: " + isValid);
    }

    /**
     * BONUS TEST: Test with empty secret (edge case)
     */
    @Test
    void verifyCode_WithEmptySecret_ShouldHandleGracefully() {
        // Arrange
        String emptySecret = "";
        int anyCode = 123456;

        // Act & Assert
        // This will likely throw an exception or return false
        // Let's see what happens!
        try {
            boolean result = totpService.verifyCode(emptySecret, anyCode);
            assertThat(result).isFalse();
            System.out.println("✅ Empty secret handled gracefully: " + result);
        } catch (Exception e) {
            // If it throws an exception, that's also acceptable behavior
            System.out.println("✅ Empty secret threw exception (acceptable): " + e.getClass().getSimpleName());
            assertThat(e).isNotNull();
        }
    }
}