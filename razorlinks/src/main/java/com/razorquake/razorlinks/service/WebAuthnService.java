package com.razorquake.razorlinks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.models.WebAuthnChallenge;
import com.razorquake.razorlinks.models.WebAuthnCredential;
import com.razorquake.razorlinks.repository.UserRepository;
import com.razorquake.razorlinks.repository.WebAuthnChallengeRepository;
import com.razorquake.razorlinks.repository.WebAuthnCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebAuthnService {

    private final WebAuthnCredentialRepository credentialRepository;
    private final WebAuthnChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${webauthn.rp.id}")
    private String rpId;

    @Value("${webauthn.rp.name}")
    private String rpName;

    @Value("${webauthn.rp.origin}")
    private String origin;


    @Transactional
    public Map<String, Object> generateRegistrationOptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        byte[] challengeBytes = new byte[32];
        new SecureRandom().nextBytes(challengeBytes);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);

        WebAuthnChallenge webAuthnChallenge = new WebAuthnChallenge();
        webAuthnChallenge.setChallenge(challenge);
        webAuthnChallenge.setUserId(user.getId().toString());
        webAuthnChallenge.setType(WebAuthnChallenge.ChallengeType.REGISTRATION);
        challengeRepository.save(webAuthnChallenge);

        List<WebAuthnCredential> existingCredentials = credentialRepository.findByUser(user);
        List<Map<String, String>> excludeCredentials = existingCredentials.stream()
                .map(cred -> Map.of("type", "public-key", "id", cred.getCredentialId()))
                .collect(Collectors.toList());

        Map<String, Object> options = new HashMap<>();
        options.put("challenge", challenge);
        options.put("rp", Map.of("name", rpName, "id", rpId));

        String userIdBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(user.getId().toString().getBytes());
        options.put("user", Map.of(
                "id", userIdBase64,
                "name", user.getEmail(),
                "displayName", user.getUsername()
        ));

        options.put("pubKeyCredParams", Arrays.asList(
                Map.of("type", "public-key", "alg", -7),
                Map.of("type", "public-key", "alg", -257)
        ));

        options.put("timeout", 60000);
        options.put("attestation", "none");
        options.put("authenticatorSelection", Map.of(
                "requireResidentKey", false,
                "residentKey", "preferred",
                "userVerification", "preferred"
        ));

        if (!excludeCredentials.isEmpty()) {
            options.put("excludeCredentials", excludeCredentials);
        }

        return options;
    }

    @Transactional
    public void verifyAndStoreCredential(String userEmail, Map<String, Object> credentialData) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String credentialId = (String) credentialData.get("id");
            String rawId = (String) credentialData.get("rawId");
            Map<String, Object> response = (Map<String, Object>) credentialData.get("response");

            String clientDataJSON = (String) response.get("clientDataJSON");
            String attestationObject = (String) response.get("attestationObject");

            String clientDataStr = new String(Base64.getUrlDecoder().decode(clientDataJSON));
            Map<String, Object> clientDataMap = objectMapper.readValue(clientDataStr, Map.class);
            String challengeFromClient = (String) clientDataMap.get("challenge");

            WebAuthnChallenge storedChallenge = challengeRepository
                    .findByUserIdAndChallengeAndType(
                            user.getId().toString(),
                            challengeFromClient,
                            WebAuthnChallenge.ChallengeType.REGISTRATION
                    )
                    .orElseThrow(() -> new RuntimeException("Challenge not found"));

            if (storedChallenge.isExpired() || storedChallenge.getUsed()) {
                throw new RuntimeException("Challenge expired or already used");
            }

            // Store credential (simplified - in production use webauthn4j for full validation)
            WebAuthnCredential credential = new WebAuthnCredential();
            credential.setCredentialId(credentialId);
            credential.setUser(user);
            credential.setSignCount(0L);
            credential.setPublicKey(attestationObject); // Store for validation
            credential.setAaguid("00000000-0000-0000-0000-000000000000");

            if (response.containsKey("transports")) {
                List<String> transports = (List<String>) response.get("transports");
                credential.setTransports(String.join(",", transports));
            }

            credentialRepository.save(credential);

            storedChallenge.setUsed(true);
            challengeRepository.save(storedChallenge);

            log.info("WebAuthn credential registered for user: {}", userEmail);

        } catch (Exception e) {
            log.error("Failed to verify and store credential", e);
            throw new RuntimeException("Failed to register credential: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> generateAuthenticationOptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WebAuthnCredential> credentials = credentialRepository.findByUser(user);

        if (credentials.isEmpty()) {
            throw new RuntimeException("No passkeys registered for this user");
        }

        byte[] challengeBytes = new byte[32];
        new SecureRandom().nextBytes(challengeBytes);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);

        WebAuthnChallenge webAuthnChallenge = new WebAuthnChallenge();
        webAuthnChallenge.setChallenge(challenge);
        webAuthnChallenge.setUserId(user.getId().toString());
        webAuthnChallenge.setType(WebAuthnChallenge.ChallengeType.AUTHENTICATION);
        challengeRepository.save(webAuthnChallenge);

        List<Map<String, Object>> allowCredentials = credentials.stream()
                .map(cred -> {
                    Map<String, Object> credMap = new HashMap<>();
                    credMap.put("type", "public-key");
                    credMap.put("id", cred.getCredentialId());
                    if (cred.getTransports() != null && !cred.getTransports().isEmpty()) {
                        credMap.put("transports", Arrays.asList(cred.getTransports().split(",")));
                    }
                    return credMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> options = new HashMap<>();
        options.put("challenge", challenge);
        options.put("timeout", 60000);
        options.put("rpId", rpId);
        options.put("allowCredentials", allowCredentials);
        options.put("userVerification", "preferred");

        return options;
    }

    @Transactional
    public User verifyAuthentication(String userEmail, Map<String, Object> credentialData) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String credentialId = (String) credentialData.get("id");
            Map<String, Object> response = (Map<String, Object>) credentialData.get("response");

            String clientDataJSON = (String) response.get("clientDataJSON");

            WebAuthnCredential storedCredential = credentialRepository
                    .findByCredentialIdAndUser(credentialId, user)
                    .orElseThrow(() -> new RuntimeException("Credential not found"));

            String clientDataStr = new String(Base64.getUrlDecoder().decode(clientDataJSON));
            Map<String, Object> clientDataMap = objectMapper.readValue(clientDataStr, Map.class);
            String challengeFromClient = (String) clientDataMap.get("challenge");

            WebAuthnChallenge storedChallenge = challengeRepository
                    .findByUserIdAndChallengeAndType(
                            user.getId().toString(),
                            challengeFromClient,
                            WebAuthnChallenge.ChallengeType.AUTHENTICATION
                    )
                    .orElseThrow(() -> new RuntimeException("Challenge not found"));

            if (storedChallenge.isExpired() || storedChallenge.getUsed()) {
                throw new RuntimeException("Challenge expired or already used");
            }

            // In production, use webauthn4j to verify signature
            // For now, simplified validation

            storedCredential.setSignCount(storedCredential.getSignCount() + 1);
            storedCredential.updateLastUsed();
            credentialRepository.save(storedCredential);

            storedChallenge.setUsed(true);
            challengeRepository.save(storedChallenge);

            log.info("WebAuthn authentication successful for user: {}", userEmail);

            return user;

        } catch (Exception e) {
            log.error("Authentication error", e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    public List<WebAuthnCredential> getUserCredentials(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return credentialRepository.findByUser(user);
    }

    @Transactional
    public void deleteCredential(String userEmail, String credentialId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WebAuthnCredential credential = credentialRepository
                .findByCredentialIdAndUser(credentialId, user)
                .orElseThrow(() -> new RuntimeException("Credential not found"));

        credentialRepository.delete(credential);
    }
}