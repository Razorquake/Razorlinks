package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.LoginRequest;
import com.razorquake.razorlinks.dtos.RegisterRequest;
import com.razorquake.razorlinks.dtos.UserDTO;
import com.razorquake.razorlinks.dtos.UserFilter;
import com.razorquake.razorlinks.exception.EmailAlreadyExistsException;
import com.razorquake.razorlinks.exception.EmailVerificationException;
import com.razorquake.razorlinks.exception.RoleNotFoundException;
import com.razorquake.razorlinks.exception.UsernameAlreadyExistsException;
import com.razorquake.razorlinks.models.AppRole;
import com.razorquake.razorlinks.models.Role;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.RoleRepository;
import com.razorquake.razorlinks.repository.UserRepository;
import com.razorquake.razorlinks.security.jwt.JwtAuthenticationResponse;
import com.razorquake.razorlinks.security.jwt.JwtUtils;
import com.razorquake.razorlinks.security.service.UserDetailsImpl;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 🧠 Testing UserService - The Brain of the Application!
 *
 * This is the most complex service to test because it has:
 * - Multiple dependencies (repositories, services)
 * - Complex business logic
 * - Exception handling
 * - Authentication logic
 * - 2FA functionality
 *
 * This is PURE MOCKITO - no Spring context needed!
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TotpService totpService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role userRole;
    private Role adminRole;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Create roles
        userRole = new Role(AppRole.ROLE_USER);
        userRole.setRoleId(1);

        adminRole = new Role(AppRole.ROLE_ADMIN);
        adminRole.setRoleId(2);

        // Create test user
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
        testUser.setRole(userRole);
        testUser.setEnabled(true);
        testUser.setSignUpMethod("email");

        // Create register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        // Create login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        System.out.println("\n🧠 === NEW USER SERVICE TEST ===");
    }

    /**
     * TEST 1: Register user - SUCCESS
     */
    @Test
    void registerUser_ValidRequest_ReturnsSuccessMessage() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(AppRole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailVerificationService).sendVerificationEmail(any(User.class));

        System.out.println("🎭 Mocking successful registration");

        // Act
        Map<String, Object> response = userService.registerUser(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.get("message")).isEqualTo("User registered successfully. Please check your email to verify your account.");
        assertThat(response.get("status")).isEqualTo(true);
        assertThat(response.get("email")).isEqualTo(registerRequest.getEmail());

        System.out.println("✅ User registered successfully");

        // Verify all interactions
        verify(userRepository, times(1)).existsByUsername(registerRequest.getUsername());
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailVerificationService, times(1)).sendVerificationEmail(any(User.class));
    }

    /**
     * TEST 2: Register user - Username already exists
     */
    @Test
    void registerUser_UsernameExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        System.out.println("🎭 Simulating username conflict");

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(registerRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username already exists");

        System.out.println("✅ Username conflict exception thrown");

        // Verify save was NEVER called
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * TEST 3: Register user - Email already exists
     */
    @Test
    void registerUser_EmailExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        System.out.println("🎭 Simulating email conflict");

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists");

        System.out.println("✅ Email conflict exception thrown");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * TEST 4: Register user - Role not found
     */
    @Test
    void registerUser_RoleNotFound_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(AppRole.ROLE_USER)).thenReturn(Optional.empty());

        System.out.println("🎭 Simulating role not found");

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(registerRequest))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Error: Role is not found.");

        System.out.println("✅ Role not found exception thrown");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * TEST 5: Register user - Default USER role assigned
     */
    @Test
    void registerUser_AssignsDefaultUserRole() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByRoleName(AppRole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailVerificationService).sendVerificationEmail(any(User.class));

        System.out.println("🎭 Registering user with default USER role");

        // Act
        Map<String, Object> response = userService.registerUser(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.get("status")).isEqualTo(true);

        // Capture the user that was saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(userRole);

        System.out.println("✅ Default USER role assigned correctly");
    }

    /**
     * TEST 6: Authenticate user - SUCCESS
     */
    @Test
    void authenticateUser_ValidCredentials_ReturnsJwtToken() {
        // Arrange
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateToken(any(UserDetailsImpl.class))).thenReturn("fake-jwt-token");

        System.out.println("🎭 Mocking successful login");

        // Act
        JwtAuthenticationResponse response = userService.authenticateUser(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getRoles()).contains("ROLE_USER");

        System.out.println("✅ User authenticated successfully");

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateToken(any(UserDetailsImpl.class));
    }

    /**
     * TEST 7: Authenticate user - Email not verified
     */
    @Test
    void authenticateUser_EmailNotVerified_ThrowsException() {
        // Arrange
        testUser.setEnabled(false);  // Email not verified

        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));

        System.out.println("🎭 Simulating unverified email");

        // Act & Assert
        assertThatThrownBy(() -> userService.authenticateUser(loginRequest))
                .isInstanceOf(EmailVerificationException.class)
                .hasMessageContaining("Please verify your email");

        System.out.println("✅ Unverified email exception thrown");

        verify(authenticationManager, never()).authenticate(any());
    }

    /**
     * TEST 8: Find by username - SUCCESS
     */
    @Test
    void findByUsername_UserExists_ReturnsUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        System.out.println("🎭 Finding user by username");

        // Act
        User foundUser = userService.findByUsername("testuser");

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");

        System.out.println("✅ User found successfully");

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    /**
     * TEST 9: Find by username - User not found
     */
    @Test
    void findByUsername_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        System.out.println("🎭 Searching for non-existent user");

        // Act & Assert
        assertThatThrownBy(() -> userService.findByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        System.out.println("✅ User not found exception thrown");
    }

    /**
     * TEST 10: Get all users with pagination and sorting
     */
    @Test
    void getAllUsers_ReturnsPagedUserDTOs() {
        // Arrange
        UserFilter filter = new UserFilter();
        filter.setUsername("test");
        filter.setPage(1);
        filter.setSize(25);
        filter.setSortBy("username");
        filter.setSortOrder("ASC");

        Page<User> users = new PageImpl<>(
                Collections.singletonList(testUser),
                PageRequest.of(1, 25, Sort.by(Sort.Direction.ASC, "username")),
                1
        );
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(users);

        System.out.println("🎭 Getting paged users with filter and sorting");

        // Act
        Page<UserDTO> result = userService.getAllUsers(filter);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserName()).isEqualTo("testuser");
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(25);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository, times(1)).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(25);
        assertThat(pageable.getSort().getOrderFor("username")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("username").getDirection()).isEqualTo(Sort.Direction.ASC);

        System.out.println("✅ Paged users retrieved successfully");
    }

    /**
     * TEST 11: Update user role - SUCCESS
     */
    @Test
    void updateUserRole_ValidRole_UpdatesRole() {
        // Arrange
        Long userId = 1L;
        String roleName = "ROLE_ADMIN";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByRoleName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        System.out.println("🎭 Updating user role to ADMIN");

        // Act
        userService.updateUserRole(userId, roleName);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(adminRole);

        System.out.println("✅ User role updated successfully");
    }

    /**
     * TEST 12: Update user role - Role not found
     */
    @Test
    void updateUserRole_RoleNotFound_ThrowsException() {
        // Arrange
        Long userId = 1L;
        String roleName = "ROLE_INVALID";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        System.out.println("🎭 Attempting to update with invalid role");

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUserRole(userId, roleName))
                .isInstanceOf(RoleNotFoundException.class);

        System.out.println("✅ Role not found exception thrown");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * TEST 13: Update account lock status
     */
    @Test
    void updateAccountLockStatus_ValidUser_UpdatesStatus() {
        // Arrange
        Long userId = 1L;
        boolean lock = true;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        System.out.println("🎭 Locking user account");

        // Act
        userService.updateAccountLockStatus(userId, lock);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isAccountNonLocked()).isFalse();  // Lock=true means NonLocked=false

        System.out.println("✅ Account locked successfully");
    }

    /**
     * TEST 14: Generate 2FA secret
     */
    @Test
    void generate2FASecret_ValidUser_GeneratesSecret() {
        // Arrange
        Long userId = 1L;
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder("SECRETKEY").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(totpService.generateSecret()).thenReturn(key);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        System.out.println("🎭 Generating 2FA secret");

        // Act
        GoogleAuthenticatorKey result = userService.generate2FASecret(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo("SECRETKEY");

        // Verify secret was saved to user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getTwoFactorSecret()).isEqualTo("SECRETKEY");

        System.out.println("✅ 2FA secret generated and saved");
    }

    /**
     * TEST 15: Validate 2FA code - Valid
     */
    @Test
    void validate2FACode_ValidCode_ReturnsTrue() {
        // Arrange
        Long userId = 1L;
        int code = 123456;
        testUser.setTwoFactorSecret("SECRETKEY");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(totpService.verifyCode("SECRETKEY", code)).thenReturn(true);

        System.out.println("🎭 Validating 2FA code");

        // Act
        boolean result = userService.validate2FACode(userId, code);

        // Assert
        assertThat(result).isTrue();

        System.out.println("✅ 2FA code validated successfully");

        verify(totpService, times(1)).verifyCode("SECRETKEY", code);
    }

    /**
     * TEST 16: Validate 2FA code - Invalid
     */
    @Test
    void validate2FACode_InvalidCode_ReturnsFalse() {
        // Arrange
        Long userId = 1L;
        int code = 999999;
        testUser.setTwoFactorSecret("SECRETKEY");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(totpService.verifyCode("SECRETKEY", code)).thenReturn(false);

        System.out.println("🎭 Testing invalid 2FA code");

        // Act
        boolean result = userService.validate2FACode(userId, code);

        // Assert
        assertThat(result).isFalse();

        System.out.println("✅ Invalid 2FA code correctly rejected");
    }

    /**
     * TEST 17: Enable 2FA
     */
    @Test
    void enable2FA_ValidUser_Enables2FA() {
        // Arrange
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        System.out.println("🎭 Enabling 2FA");

        // Act
        userService.enable2FA(userId);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isTwoFactorEnabled()).isTrue();

        System.out.println("✅ 2FA enabled successfully");
    }

    /**
     * TEST 18: Disable 2FA
     */
    @Test
    void disable2FA_ValidUser_Disables2FA() {
        // Arrange
        Long userId = 1L;
        testUser.setTwoFactorEnabled(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        System.out.println("🎭 Disabling 2FA");

        // Act
        userService.disable2FA(userId);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isTwoFactorEnabled()).isFalse();

        System.out.println("✅ 2FA disabled successfully");
    }

    /**
     * TEST 19: Update account expiry status
     */
    @Test
    void updateAccountExpiryStatus_ValidUser_UpdatesStatus() {
        // Arrange
        Long userId = 1L;
        boolean expiry = true;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updateAccountExpiryStatus(userId, expiry);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isAccountNonExpired()).isFalse();

        System.out.println("✅ Account expiry status updated");
    }

    /**
     * TEST 20: Update account enabled status
     */
    @Test
    void updateAccountEnabledStatus_ValidUser_UpdatesStatus() {
        // Arrange
        Long userId = 1L;
        boolean enabled = false;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updateAccountEnabledStatus(userId, enabled);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isEnabled()).isFalse();

        System.out.println("✅ Account enabled status updated");
    }

    /**
     * TEST 21: Update credentials expiry status
     */
    @Test
    void updateCredentialsExpiryStatus_ValidUser_UpdatesStatus() {
        // Arrange
        Long userId = 1L;
        boolean expiry = true;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.updateCredentialsExpiryStatus(userId, expiry);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.isCredentialsNonExpired()).isFalse();

        System.out.println("✅ Credentials expiry status updated");
    }

    /**
     * TEST 22: Update password
     */
    @Test
    void updatePassword_ValidUser_UpdatesPassword() {
        // Arrange
        Long userId = 1L;
        String newPassword = "newPassword123";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded-new-password");

        // Act
        userService.updatePassword(userId, newPassword);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encoded-new-password");

        System.out.println("✅ Password updated successfully");
    }

    /**
     * TEST 23: Get user by ID
     */
    @Test
    void getUserById_UserExists_ReturnsUserDto() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserById(testUser.getId());

        // Assert
        assertThat(result.getUserName()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        System.out.println("✅ User retrieved by ID");
    }

    /**
     * TEST 24: Get user by ID - not found
     */
    @Test
    void getUserById_UserMissing_ThrowsException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        System.out.println("✅ Missing user throws exception");
    }

    /**
     * TEST 25: Get all roles
     */
    @Test
    void getAllRoles_ReturnsRoles() {
        // Arrange
        List<Role> roles = List.of(userRole, adminRole);
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        List<Role> result = userService.getAllRoles();

        // Assert
        assertThat(result).containsExactly(userRole, adminRole);

        System.out.println("✅ Roles retrieved successfully");
    }

    /**
     * TEST 26: Logged in user
     */
    @Test
    void loggedInUser_AuthenticationPresent_ReturnsUser() {
        // Arrange
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("testuser", "password"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        try {
            // Act
            User result = userService.loggedInUser();

            // Assert
            assertThat(result).isEqualTo(testUser);
            verify(userRepository).findByUsername("testuser");

            System.out.println("✅ Logged in user resolved successfully");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * TEST 27: Update user role - valid name but repository missing
     */
    @Test
    void updateUserRole_ValidRoleMissingInRepository_ThrowsException() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByRoleName(AppRole.ROLE_ADMIN)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUserRole(userId, "ROLE_ADMIN"))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found");

        verify(userRepository, never()).save(any(User.class));
        System.out.println("✅ Missing role throws exception");
    }
}
