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
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final EmailService emailService;
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    public void sendPasswordResetEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isEnabled()) {
                throw new EmailVerificationException("Please verify your email address before logging in. Check your email for verification link.");
            }
            try {
                // Delete any existing tokens for this user
                tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
                tokenRepository.flush();

                // Generate new token
                String token = java.util.UUID.randomUUID().toString();
                Instant expiresAt = Instant.now().plus(1, java.time.temporal.ChronoUnit.HOURS); // 1-hour expiration

                PasswordResetToken resetToken = new PasswordResetToken(token, user, expiresAt);
                tokenRepository.save(resetToken);

                // Send password reset email
                String resetUrl = frontendUrl + "/reset-password?token=" + token;
                emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
            } catch (MessagingException e) {
                log.error("Failed to send password reset email to: {}", user.getEmail(), e);
                throw new EmailVerificationException("Failed to send password reset email. Please try again later.");
            }
        });
    }

    // Clean up expired tokens daily at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredAndVerifiedTokens(Instant.now());
        log.info("Cleaned up expired verification tokens");
    }

    @Transactional
    public void resetPassword(@Valid PasswordResetRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Verification token has expired");
        }

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Token has already been used");
        }

        resetToken.setUsedAt(Instant.now());
        tokenRepository.save(resetToken);

        User user = resetToken.getUser();

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new SamePasswordException("New password must be different from the old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getUsername());
    }
}
