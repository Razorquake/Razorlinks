package com.razorquake.razorlinks.security.jwt;

import com.razorquake.razorlinks.models.AppRole;
import com.razorquake.razorlinks.models.Role;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.security.service.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();

        String secret = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("test-secret-key-for-jwt-tests-1234567890123456"
                        .getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000);

        Role role = new Role(AppRole.ROLE_USER);
        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);
        user.setRole(role);
        user.setTwoFactorEnabled(true);
        userDetails = UserDetailsImpl.build(user);
    }

    @Test
    void generateToken_ParsesUsernameAndValidates() {
        String token = jwtUtils.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("testuser");
        assertThat(jwtUtils.validateToken(token)).isTrue();
    }

    @Test
    void getJwtFromHeader_WithBearerPrefix_ReturnsToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-value");

        assertThat(jwtUtils.getJwtFromHeader(request)).isEqualTo("token-value");
    }

    @Test
    void getJwtFromHeader_NoHeader_ReturnsNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThat(jwtUtils.getJwtFromHeader(request)).isNull();
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        assertThat(jwtUtils.validateToken("invalid.token.value")).isFalse();
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -60000);
        String token = jwtUtils.generateToken(userDetails);

        assertThat(jwtUtils.validateToken(token)).isFalse();
    }
}
