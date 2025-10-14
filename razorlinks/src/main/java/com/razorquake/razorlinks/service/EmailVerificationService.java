package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.exception.EmailVerificationException;
import com.razorquake.razorlinks.exception.InvalidTokenException;
import com.razorquake.razorlinks.models.EmailVerificationToken;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.EmailVerificationTokenRepository;
import com.razorquake.razorlinks.repository.UserRepository;
import com.razorquake.razorlinks.security.jwt.JwtAuthenticationResponse;
import com.razorquake.razorlinks.security.jwt.JwtUtils;
import com.razorquake.razorlinks.security.service.UserDetailsImpl;
import com.razorquake.razorlinks.security.util.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailService emailService;
    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    public void sendVerificationEmail(User user) {
        // Delete any existing tokens for this user
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        tokenRepository.flush();

        // Generate new token
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS); // 24-hour expiration

        EmailVerificationToken verificationToken = new EmailVerificationToken(token, user, expiresAt);
        tokenRepository.save(verificationToken);

        // Send verification email
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationUrl);

        log.info("Verification email sent to: {}", user.getEmail());
    }

    @Transactional
    public JwtAuthenticationResponse verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new InvalidTokenException("Verification token has expired");
        }

        if (verificationToken.isVerified()) {
            throw new InvalidTokenException("Email has already been verified");
        }

        // Mark token as verified
        verificationToken.setVerifiedAt(Instant.now());
        tokenRepository.save(verificationToken);

        // Enable user account
        User user = verificationToken.getUser();
        user.setEnabled(true);
        User savedUser = userRepository.save(user);

        log.info("Email verified for user: {}", user.getUsername());

        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);
        String jwt = jwtUtils.generateToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new JwtAuthenticationResponse(jwt, roles);


    }

    @Transactional
    public void resendVerificationEmail(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EmailVerificationException("User not found with username: " + username));

        if (user.isEnabled()) {
            throw new EmailVerificationException("Username is already verified");
        }

        sendVerificationEmail(user);
    }

    // Clean up expired tokens daily at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredAndVerifiedTokens(Instant.now());
        log.info("Cleaned up expired and used verification tokens");
    }
}
