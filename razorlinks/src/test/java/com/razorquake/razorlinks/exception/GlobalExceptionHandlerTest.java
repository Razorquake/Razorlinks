package com.razorquake.razorlinks.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAuthenticationException_ReturnsUnauthorized() {
        ResponseEntity<Map<String, Object>> response = handler.handleAuthenticationException(
                new BadCredentialsException("bad")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("message", "Bad credentials");
    }

    @Test
    void handleUsernameAlreadyExists_ReturnsConflict() {
        ResponseEntity<Map<String, Object>> response = handler.handleUsernameAlreadyExists(
                new UsernameAlreadyExistsException("Username already exists")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "Username already exists");
    }

    @Test
    void handleEmailAlreadyExists_ReturnsConflict() {
        ResponseEntity<Map<String, Object>> response = handler.handleEmailAlreadyExists(
                new EmailAlreadyExistsException("Email already exists")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "Email already exists");
    }

    @Test
    void handleRoleNotFound_ReturnsNotFound() {
        ResponseEntity<Map<String, Object>> response = handler.handleRoleNotFound(
                new RoleNotFoundException("Role missing")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "Role missing");
    }

    @Test
    void handleEmailVerificationException_ReturnsBadRequest() {
        ResponseEntity<Map<String, Object>> response = handler.handleEmailVerificationException(
                new EmailVerificationException("Verification failed")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Verification failed");
    }

    @Test
    void handleEmailNotFoundException_ReturnsNotFound() {
        ResponseEntity<Map<String, Object>> response = handler.handleEmailNotFoundException(
                new EmailNotFoundException("Email not found")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "Email not found");
    }

    @Test
    void handleInvalidTokenException_ReturnsBadRequest() {
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidTokenException(
                new InvalidTokenException("Invalid token")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Invalid token");
    }

    @Test
    void handleSamePasswordException_ReturnsBadRequest() {
        ResponseEntity<Map<String, Object>> response = handler.handleSamePasswordException(
                new SamePasswordException("Same password")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Same password");
    }
}
