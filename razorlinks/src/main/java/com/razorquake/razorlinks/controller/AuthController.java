package com.razorquake.razorlinks.controller;

import com.razorquake.razorlinks.dtos.*;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.models.WebAuthnCredential;
import com.razorquake.razorlinks.security.jwt.JwtAuthenticationResponse;
import com.razorquake.razorlinks.security.jwt.JwtUtils;
import com.razorquake.razorlinks.security.service.UserDetailsImpl;
import com.razorquake.razorlinks.service.*;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;
    private final JwtUtils jwtUtils;
    private final TotpService totpService;
    private final WebAuthnService webAuthnService;

    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest){
        log.info("{}", registerRequest);
        return ResponseEntity.ok(userService.registerUser(registerRequest));
    }

    @PostMapping("/public/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){


        return ResponseEntity.ok(userService.authenticateUser(loginRequest));
    }

    @GetMapping("/public/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        JwtAuthenticationResponse jwtResponse = emailVerificationService.verifyEmail(token);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Email verified successfully! You are now logged in.");
        response.put("status", true);
        response.put("token", jwtResponse.getToken());
        response.put("roles", jwtResponse.getRoles());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody ResendVerificationRequest request) {
        emailVerificationService.resendVerificationEmail(request.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Verification email sent successfully. Please check your email.");
        response.put("status", true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isAccountNonLocked(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isEnabled(),
                user.getCredentialsExpiryDate(),
                user.getAccountExpiryDate(),
                user.isTwoFactorEnabled(),
                roles
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsername(Principal principal) {
        return ResponseEntity.ok(principal.getName());
    }

    @PostMapping("/public/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email){
        passwordResetService.sendPasswordResetEmail(email);
        return ResponseEntity.ok(Map.of("message", "Password reset email sent to " + email, "status", true));
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequest request){
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully", "status", true));
    }

    // 2FA Authentication
    @PostMapping("/enable-2fa")
    public ResponseEntity<String> enable2FA() {
        Long userId = userService.loggedInUser().getId();
        GoogleAuthenticatorKey secret = userService.generate2FASecret(userId);
        String qrCodeUrl = totpService.getQrCodeUrl(secret,
                userService.getUserById(userId).getUserName());
        return ResponseEntity.ok(qrCodeUrl);
    }

    @PostMapping("/disable-2fa")
    public ResponseEntity<String> disable2FA() {
        Long userId = userService.loggedInUser().getId();
        userService.disable2FA(userId);
        return ResponseEntity.ok("2FA disabled");
    }


    @PostMapping("/verify-2fa")
    public ResponseEntity<String> verify2FA(@RequestParam int code) {
        Long userId = userService.loggedInUser().getId();
        boolean isValid = userService.validate2FACode(userId, code);
        if (isValid) {
            userService.enable2FA(userId);
            return ResponseEntity.ok("2FA Verified");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid 2FA Code");
        }
    }


    @GetMapping("/user/2fa-status")
    public ResponseEntity<?> get2FAStatus() {
        User user = userService.loggedInUser();
        if (user != null){
            return ResponseEntity.ok().body(Map.of("is2faEnabled", user.isTwoFactorEnabled()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }
    }


    @PostMapping("/public/verify-2fa-login")
    public ResponseEntity<String> verify2FALogin(@RequestParam int code,
                                                 @RequestParam String jwtToken) {
        String username = jwtUtils.getUserNameFromJwtToken(jwtToken);
        User user = userService.findByUsername(username);
        boolean isValid = userService.validate2FACode(user.getId(), code);
        if (isValid) {
            return ResponseEntity.ok("2FA Verified");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid 2FA Code");
        }
    }

    // webAuthn related code
    @PostMapping("/webauthn/register/options")
    public ResponseEntity<?> getWebAuthnRegistrationOptions(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            Map<String, Object> options = webAuthnService.generateRegistrationOptions(user.getEmail());
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            log.error("Failed to generate registration options", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/webauthn/register/verify")
    public ResponseEntity<?> verifyWebAuthnRegistration(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> credentialData) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            webAuthnService.verifyAndStoreCredential(user.getEmail(), credentialData);
            return ResponseEntity.ok(Map.of("success", true, "message", "Passkey registered successfully"));
        } catch (Exception e) {
            log.error("Failed to verify registration", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/webauthn/credentials")
    public ResponseEntity<?> getWebAuthnCredentials(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            List<WebAuthnCredential> credentials = webAuthnService.getUserCredentials(user.getEmail());

            List<Map<String, Object>> credentialInfo = credentials.stream()
                    .map(cred -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("id", cred.getId());
                        info.put("credentialId", cred.getCredentialId());
                        info.put("credentialName", cred.getCredentialName());
                        info.put("createdAt", cred.getCreatedAt());
                        info.put("lastUsedAt", cred.getLastUsedAt());
                        info.put("transports", cred.getTransports());
                        return info;
                    })
                    .toList();

            return ResponseEntity.ok(credentialInfo);
        } catch (Exception e) {
            log.error("Failed to get credentials", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/webauthn/credentials/{credentialId}")
    public ResponseEntity<?> deleteWebAuthnCredential(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String credentialId) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            webAuthnService.deleteCredential(user.getEmail(), credentialId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Passkey deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete credential", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Authentication endpoints (public)
    @PostMapping("/public/webauthn/authenticate/options")
    public ResponseEntity<?> getWebAuthnAuthenticationOptions(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            Map<String, Object> options = webAuthnService.generateAuthenticationOptions(email);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            log.error("Failed to generate authentication options", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/public/webauthn/authenticate/verify")
    public ResponseEntity<?> verifyWebAuthnAuthentication(@RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");
            Map<String, Object> credential = (Map<String, Object>) request.get("credential");

            if (email == null || credential == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email and credential are required"));
            }

            User user = webAuthnService.verifyAuthentication(email, credential);

            // Generate JWT token (reuse existing logic)
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            String jwt = jwtUtils.generateToken(userDetails);

            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt,
                    userDetails.getAuthorities().stream()
                            .map(a -> a.getAuthority())
                            .toList()));
        } catch (Exception e) {
            log.error("Authentication failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }

    @GetMapping("/public/webauthn/has-credentials")
    public ResponseEntity<?> hasWebAuthnCredentials(@RequestParam String email) {
        try {
            List<WebAuthnCredential> credentials = webAuthnService.getUserCredentials(email);
            return ResponseEntity.ok(Map.of(
                    "hasCredentials", !credentials.isEmpty(),
                    "count", credentials.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("hasCredentials", false, "count", 0));
        }
    }
}
