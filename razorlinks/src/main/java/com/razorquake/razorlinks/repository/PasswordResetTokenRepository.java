package com.razorquake.razorlinks.repository;

import com.razorquake.razorlinks.models.PasswordResetToken;
import com.razorquake.razorlinks.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    @Modifying
    @Query("DELETE FROM PasswordResetToken e WHERE e.expiresAt < :now OR e.usedAt IS NOT NULL")
    void deleteExpiredAndVerifiedTokens(Instant now);
}