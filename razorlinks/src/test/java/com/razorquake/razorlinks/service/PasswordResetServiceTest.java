package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.PasswordResetRequest;
import com.razorquake.razorlinks.exception.EmailVerificationException;
import com.razorquake.razorlinks.exception.InvalidTokenException;
import com.razorquake.razorlinks.exception.SamePasswordException;
import com.razorquake.razorlinks.models.PasswordResetToken;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.PasswordResetTokenRepository;
import com.razorquake.razorlinks.repository.UserRepository;
import com.razorquake.razorlinks.security.util.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "encoded-old");
        user.setId(1L);
        user.setEnabled(true);

        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://frontend");
    }

    @Test
    void sendPasswordResetEmail_UserNotFound_NoEmailSent() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        passwordResetService.sendPasswordResetEmail("missing@example.com");

        verify(userRepository).findByEmail("missing@example.com");
        verifyNoInteractions(tokenRepository, emailService);
    }

    @Test
    void sendPasswordResetEmail_UnverifiedUser_ThrowsException() {
        user.setEnabled(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> passwordResetService.sendPasswordResetEmail(user.getEmail()))
                .isInstanceOf(EmailVerificationException.class)
                .hasMessageContaining("Please verify your email");

        verifyNoInteractions(tokenRepository, emailService);
    }

    @Test
    void sendPasswordResetEmail_ValidUser_SavesTokenAndSendsEmail() {
        PasswordResetToken existingToken = new PasswordResetToken(
                "old-token",
                user,
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser(user)).thenReturn(Optional.of(existingToken));

        Instant beforeCall = Instant.now();

        passwordResetService.sendPasswordResetEmail(user.getEmail());

        verify(tokenRepository).delete(existingToken);
        verify(tokenRepository).flush();

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getToken()).isNotBlank();
        assertThat(savedToken.getExpiresAt()).isAfter(beforeCall);

        verify(emailService).sendPasswordResetEmail(
                eq(user.getEmail()),
                eq(user.getUsername()),
                eq("http://frontend/reset-password?token=" + savedToken.getToken())
        );
    }

    @Test
    void resetPassword_InvalidToken_ThrowsException() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken("missing");
        request.setNewPassword("newpass123");

        when(tokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid password reset token");
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsException() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken("expired-token");
        request.setNewPassword("newpass123");

        PasswordResetToken token = new PasswordResetToken(
                "expired-token",
                user,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );
        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Verification token has expired");
    }

    @Test
    void resetPassword_UsedToken_ThrowsException() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken("used-token");
        request.setNewPassword("newpass123");

        PasswordResetToken token = new PasswordResetToken(
                "used-token",
                user,
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
        token.setUsedAt(Instant.now());

        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Token has already been used");
    }

    @Test
    void resetPassword_SamePassword_ThrowsException() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken("same-token");
        request.setNewPassword("newpass123");

        PasswordResetToken token = new PasswordResetToken(
                "same-token",
                user,
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
        when(tokenRepository.findByToken("same-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> passwordResetService.resetPassword(request))
                .isInstanceOf(SamePasswordException.class)
                .hasMessageContaining("New password must be different");

        verify(tokenRepository).save(token);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_ValidRequest_UpdatesPassword() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setToken("valid-token");
        request.setNewPassword("newpass123");

        PasswordResetToken token = new PasswordResetToken(
                "valid-token",
                user,
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encoded-new");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        passwordResetService.resetPassword(request);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getUsedAt()).isNotNull();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-new");
    }

    @Test
    void cleanupExpiredTokens_DelegatesToRepository() {
        passwordResetService.cleanupExpiredTokens();

        verify(tokenRepository).deleteExpiredAndVerifiedTokens(any(Instant.class));
    }
}
