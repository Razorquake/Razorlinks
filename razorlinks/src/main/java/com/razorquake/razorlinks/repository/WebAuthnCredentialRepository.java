package com.razorquake.razorlinks.repository;

import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.models.WebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, Long> {

    List<WebAuthnCredential> findByUser(User user);

    Optional<WebAuthnCredential> findByCredentialId(String credentialId);

    Optional<WebAuthnCredential> findByCredentialIdAndUser(String credentialId, User user);

    Long countByUser(User user);
}