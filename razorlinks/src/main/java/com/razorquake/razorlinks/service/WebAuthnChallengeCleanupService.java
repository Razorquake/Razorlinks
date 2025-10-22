package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.repository.WebAuthnChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebAuthnChallengeCleanupService {

    private final WebAuthnChallengeRepository challengeRepository;

    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void cleanupExpiredChallenges() {
        try {
            challengeRepository.deleteByExpiresAtBefore(LocalDateTime.now());
            log.info("Cleaned up expired WebAuthn challenges");
        } catch (Exception e) {
            log.error("Failed to cleanup expired challenges", e);
        }
    }

    @Scheduled(fixedRate = 21600000) // Every 6 hours
    @Transactional
    public void cleanupUsedChallenges() {
        try {
            challengeRepository.deleteByUsedTrue();
            log.info("Cleaned up used WebAuthn challenges");
        } catch (Exception e) {
            log.error("Failed to cleanup used challenges", e);
        }
    }
}