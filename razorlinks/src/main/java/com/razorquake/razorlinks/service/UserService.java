package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.LoginRequest;
import com.razorquake.razorlinks.dtos.RegisterRequest;
import com.razorquake.razorlinks.dtos.UserDTO;
import com.razorquake.razorlinks.exception.*;
import com.razorquake.razorlinks.models.AppRole;
import com.razorquake.razorlinks.models.Role;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.RoleRepository;
import com.razorquake.razorlinks.repository.UserRepository;
import com.razorquake.razorlinks.security.jwt.JwtAuthenticationResponse;
import com.razorquake.razorlinks.security.jwt.JwtUtils;
import com.razorquake.razorlinks.security.service.UserDetailsImpl;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;
    private final TotpService totpService;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public Map<String, Object> registerUser(RegisterRequest registerRequest){
        if(userRepository.existsByUsername(registerRequest.getUsername()))
            throw new UsernameAlreadyExistsException("Username already exists");
        if (userRepository.existsByEmail(registerRequest.getEmail()))
            throw new EmailAlreadyExistsException("Email already exists");

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        Set<String> strRoles = registerRequest.getRole();
        Role role;
        if (strRoles == null || strRoles.isEmpty()) {
            role = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RoleNotFoundException("Error: Role is not found."));
        } else {
            String roleStr = strRoles.iterator().next();
            if (roleStr.equals("admin")) {
                role = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                        .orElseThrow(() -> new RoleNotFoundException("Error: Role is not found."));
            } else {
                role = roleRepository.findByRoleName(AppRole.ROLE_USER)
                        .orElseThrow(() -> new RoleNotFoundException("Error: Role is not found."));
            }
        }
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setSignUpMethod("email");
        user.setEnabled(false);
        User savedUser = userRepository.save(user);

        emailVerificationService.sendVerificationEmail(savedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully. Please check your email to verify your account.");
        response.put("email", savedUser.getEmail());
        response.put("status", true);

        return response;
    }

    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest){

        // First check if user exists and is enabled
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + loginRequest.getUsername()));


        if (!user.isEnabled()) {
            throw new EmailVerificationException("Please verify your email address before logging in. Check your email for verification link.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        // Set the authenticated user in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);

        // Collect roles from the UserDetails
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Return the response entity with the JWT token included in the response body
        return new JwtAuthenticationResponse(jwt, roles);
    }

    public User findByUsername(String name) {
        return userRepository.findByUsername(name).orElseThrow(
                                () -> new UsernameNotFoundException("User not found with username: "+name)
        );
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public User loggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + currentUsername));
    }

    public UserDTO convertToDto(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isAccountNonLocked(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isEnabled(),
                user.getCredentialsExpiryDate(),
                user.getAccountExpiryDate(),
                user.getTwoFactorSecret(),
                user.isTwoFactorEnabled(),
                user.getSignUpMethod(),
                user.getRole(),
                user.getCreatedDate(),
                user.getUpdatedDate()
        );
    }

    public void updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        AppRole appRole = AppRole.valueOf(roleName);
        Role role = roleRepository.findByRoleName(appRole)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    public UserDTO getUserById(Long id) {
        return convertToDto(userRepository.findById(id).orElseThrow(()
                -> new RuntimeException("User not found")));
    }

    public void updateAccountLockStatus(Long userId, boolean lock) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        user.setAccountNonLocked(!lock);
        userRepository.save(user);
    }

    public void updateAccountExpiryStatus(Long userId, boolean expiry) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        user.setAccountNonExpired(!expiry);
        userRepository.save(user);
    }

    public void updateCredentialsExpiryStatus(Long userId, boolean expiry) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found")
        );
        user.setCredentialsNonExpired(!expiry);
        userRepository.save(user);
    }

    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found")
        );
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public void updateAccountEnabledStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    public GoogleAuthenticatorKey generate2FASecret(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        GoogleAuthenticatorKey key = totpService.generateSecret();
        user.setTwoFactorSecret(key.getKey());
        userRepository.save(user);
        return key;
    }

    public boolean validate2FACode(Long userId, int code){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return totpService.verifyCode(user.getTwoFactorSecret(), code);
    }

    public void enable2FA(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    public void disable2FA(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
    }
}
