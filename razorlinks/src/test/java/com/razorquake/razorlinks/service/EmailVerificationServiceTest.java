package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.exception.EmailVerificationException;
import com.razorquake.razorlinks.exception.InvalidTokenException;
import com.razorquake.razorlinks.models.AppRole;
import com.razorquake.razorlinks.models.EmailVerificationToken;
import com.razorquake.razorlinks.models.Role;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.EmailVerificationTokenRepository;
import com.razorquake.razorlinks.repository.UserRepository;
import com.razorquake.razorlinks.security.jwt.JwtAuthenticationResponse;
import com.razorquake.razorlinks.security.jwt.JwtUtils;
import com.razorquake.razorlinks.security.service.UserDetailsImpl;
import com.razorquake.razorlinks.security.util.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User user;

    @BeforeEach
    void setUp() {
        Role userRole = new Role(AppRole.ROLE_USER);
        user = new User("testuser", "test@example.com", "encodedPassword");
        user.setId(1L);
        user.setRole(userRole);
        user.setEnabled(false);

        ReflectionTestUtils.setField(emailVerificationService, "frontendUrl", "http://frontend");
    }

    @Test
    void sendVerificationEmail_ValidUser_SavesTokenAndSendsEmail() {
        EmailVerificationToken existingToken = new EmailVerificationToken(
                "old-token",
                user,
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
        when(tokenRepository.findByUser(user)).thenReturn(Optional.of(existingToken));

        Instant beforeCall = Instant.now();

        emailVerificationService.sendVerificationEmail(user);

        verify(tokenRepository).delete(existingToken);
        verify(tokenRepository).flush();

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getToken()).isNotBlank();
        assertThat(savedToken.getExpiresAt()).isAfter(beforeCall);

        verify(emailService).sendVerificationEmail(
                eq(user.getEmail()),
                eq(user.getUsername()),
                eq("http://frontend/verify-email?token=" + savedToken.getToken())
        );
    }

    @Test
    void verifyEmail_InvalidToken_ThrowsException() {
        when(tokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailVerificationService.verifyEmail("missing"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid verification token");
    }

    @Test
    void verifyEmail_ExpiredToken_ThrowsException() {
        EmailVerificationToken token = new EmailVerificationToken(
                "expired-token",
                user,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );
        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> emailVerificationService.verifyEmail("expired-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Verification token has expired");
    }

    @Test
    void verifyEmail_AlreadyVerifiedToken_ThrowsException() {
        EmailVerificationToken token = new EmailVerificationToken(
                "verified-token",
                user,
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
        token.setVerifiedAt(Instant.now());
        when(tokenRepository.findByToken("verified-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> emailVerificationService.verifyEmail("verified-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Email has already been verified");
    }

    @Test
    void verifyEmail_ValidToken_EnablesUserAndReturnsJwt() {
        EmailVerificationToken token = new EmailVerificationToken(
                "valid-token",
                user,
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtils.generateToken(any(UserDetailsImpl.class))).thenReturn("jwt-token");

        JwtAuthenticationResponse response = emailVerificationService.verifyEmail("valid-token");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");

        assertThat(token.getVerifiedAt()).isNotNull();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().isEnabled()).isTrue();
    }

    @Test
    void resendVerificationEmail_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailVerificationService.resendVerificationEmail("missing"))
                .isInstanceOf(EmailVerificationException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void resendVerificationEmail_AlreadyVerified_ThrowsException() {
        user.setEnabled(true);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> emailVerificationService.resendVerificationEmail(user.getUsername()))
                .isInstanceOf(EmailVerificationException.class)
                .hasMessageContaining("already verified");
    }

    @Test
    void resendVerificationEmail_UnverifiedUser_SendsEmail() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser(user)).thenReturn(Optional.empty());

        emailVerificationService.resendVerificationEmail(user.getUsername());

        verify(emailService).sendVerificationEmail(eq(user.getEmail()), eq(user.getUsername()), any(String.class));
    }

    @Test
    void cleanupExpiredTokens_DelegatesToRepository() {
        emailVerificationService.cleanupExpiredTokens();

        verify(tokenRepository).deleteExpiredAndVerifiedTokens(any(Instant.class));
    }
}
