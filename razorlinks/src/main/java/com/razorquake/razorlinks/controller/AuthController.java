package com.razorquake.razorlinks.controller;

import com.razorquake.razorlinks.dtos.*;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.security.jwt.JwtAuthenticationResponse;
import com.razorquake.razorlinks.service.EmailVerificationService;
import com.razorquake.razorlinks.service.PasswordResetService;
import com.razorquake.razorlinks.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/user/2fa-status")
    public ResponseEntity<?> get2FAStatus() {
        User user = userService.loggedInUser();
        return ResponseEntity.ok().body(Map.of("is2faEnabled", user.isTwoFactorEnabled()));

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
}
