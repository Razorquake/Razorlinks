package com.razorquake.razorlinks.repository;

import com.razorquake.razorlinks.models.WebAuthnChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebAuthnChallengeRepository extends JpaRepository<WebAuthnChallenge, Long> {

    Optional<WebAuthnChallenge> findByChallenge(String challenge);

    Optional<WebAuthnChallenge> findByUserIdAndChallengeAndType(
            String userId,
            String challenge,
            WebAuthnChallenge.ChallengeType type
    );

    List<WebAuthnChallenge> findByUserId(String userId);

    @Modifying
    @Query("DELETE FROM WebAuthnChallenge w WHERE w.expiresAt < :dateTime")
    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM WebAuthnChallenge w WHERE w.used = true")
    void deleteByUsedTrue();
}