package com.razorquake.razorlinks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorquake.razorlinks.dtos.UserDTO;
import com.razorquake.razorlinks.models.AppRole;
import com.razorquake.razorlinks.models.Role;
import com.razorquake.razorlinks.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🔐 Testing AdminController - Role-Based Access Control!
 *
 * This controller requires ADMIN role for ALL endpoints.
 * We'll test:
 * - Admin can access ✅
 * - Regular users CANNOT access ❌
 * - All admin operations work correctly
 */
@SpringBootTest
@AutoConfigureMockMvc
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // Mock beans required by the application context but not used in these tests
    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    private UserDTO testUserDTO;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Create roles
        adminRole = new Role(AppRole.ROLE_ADMIN);
        adminRole.setRoleId(1);

        userRole = new Role(AppRole.ROLE_USER);
        userRole.setRoleId(2);

        // Create test user DTO
        testUserDTO = new UserDTO(
                1L,
                "testuser",
                "test@example.com",
                true,  // accountNonLocked
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // enabled
                LocalDate.now().plusMonths(6),  // credentialsExpiryDate
                LocalDate.now().plusYears(1),   // accountExpiryDate
                null,  // twoFactorSecret
                false, // isTwoFactorEnabled
                "email",
                userRole,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        System.out.println("\n🔐 === NEW ADMIN CONTROLLER TEST ===");
    }

    /**
     * TEST 1: Get all users - Admin role SUCCESS
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllUsers_AdminRole_ReturnsUserList() throws Exception {
        // Arrange
        List<UserDTO> users = Collections.singletonList(testUserDTO);

        when(userService.getAllUsers()).thenReturn(users);

        System.out.println("🎭 Mocked getAllUsers() with 1 user");

        // Act & Assert
        mockMvc.perform(
                        get("/api/admin/get-users")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userName").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));

        System.out.println("✅ Admin retrieved all users successfully");

        verify(userService, times(1)).getAllUsers();
    }

    /**
     * TEST 2: Get all users - Regular USER role DENIED
     */
    @Test
    @WithMockUser(username = "regularuser", roles = "USER")
    void getAllUsers_UserRole_ReturnsForbidden() throws Exception {
        // Arrange
        System.out.println("🔒 Regular user trying to access admin endpoint");

        // Act & Assert
        mockMvc.perform(
                        get("/api/admin/get-users")
                )
                .andDo(print())
                .andExpect(status().isForbidden());  // 403 Forbidden

        System.out.println("✅ Regular user correctly denied access");

        // Verify service was NEVER called
        verify(userService, never()).getAllUsers();
    }

    /**
     * TEST 3: Get all users - No authentication DENIED
     */
    @Test
    void getAllUsers_NotAuthenticated_ReturnsUnauthorized() throws Exception {
        // No @WithMockUser annotation
        System.out.println("🔒 Unauthenticated request to admin endpoint");

        // Act & Assert
        mockMvc.perform(
                        get("/api/admin/get-users")
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());  // 401 Unauthorized

        System.out.println("✅ Unauthenticated user correctly denied");

        verify(userService, never()).getAllUsers();
    }

    /**
     * TEST 4: Update user role - SUCCESS
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUserRole_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        String newRole = "ROLE_ADMIN";

        doNothing().when(userService).updateUserRole(userId, newRole);

        System.out.println("🎭 Updating user role to ADMIN");

        // Act & Assert
        mockMvc.perform(
                        put("/api/admin/update-role")
                                .param("userId", userId.toString())
                                .param("roleName", newRole)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("User role updated"));

        System.out.println("✅ User role updated successfully");

        verify(userService, times(1)).updateUserRole(userId, newRole);
    }

    /**
     * TEST 5: Get user by ID - SUCCESS
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUser_ValidId_ReturnsUser() throws Exception {
        // Arrange
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(testUserDTO);

        System.out.println("🎭 Getting user by ID: " + userId);

        // Act & Assert
        mockMvc.perform(
                        get("/api/admin/user/{id}", userId)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        System.out.println("✅ User retrieved by ID successfully");

        verify(userService, times(1)).getUserById(userId);
    }

    /**
     * TEST 6: Update account lock status - SUCCESS
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateAccountLockStatus_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        boolean lockStatus = true;

        doNothing().when(userService).updateAccountLockStatus(userId, lockStatus);

        System.out.println("🎭 Locking user account: " + userId);

        // Act & Assert
        mockMvc.perform(
                        put("/api/admin/update-lock-status")
                                .param("userId", userId.toString())
                                .param("lock", String.valueOf(lockStatus))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("User lock status updated"));

        System.out.println("✅ Account lock status updated");

        verify(userService, times(1)).updateAccountLockStatus(userId, lockStatus);
    }

    /**
     * TEST 7: Get all roles - SUCCESS
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllRoles_AdminRole_ReturnsRoleList() throws Exception {
        // Arrange
        List<Role> roles = Arrays.asList(adminRole, userRole);

        when(userService.getAllRoles()).thenReturn(roles);

        System.out.println("🎭 Getting all available roles");

        // Act & Assert
        mockMvc.perform(
                        get("/api/admin/roles")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].roleName").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[1].roleName").value("ROLE_USER"));

        System.out.println("✅ All roles retrieved successfully");

        verify(userService, times(1)).getAllRoles();
    }

    /**
     * TEST 8: Update account expiry status - SUCCESS
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateAccountExpiryStatus_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        boolean expireStatus = true;

        doNothing().when(userService).updateAccountExpiryStatus(userId, expireStatus);

        System.out.println("🎭 Expiring user account: " + userId);

        // Act & Assert
        mockMvc.perform(
                        put("/api/admin/update-expiry-status")
                                .param("userId", userId.toString())
                                .param("expire", String.valueOf(expireStatus))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Account expiry status updated"));

        System.out.println("✅ Account expiry status updated");

        verify(userService, times(1)).updateAccountExpiryStatus(userId, expireStatus);
    }

    /**
     * TEST 9: Update account enabled status - SUCCESS
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateAccountEnabledStatus_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        boolean enabledStatus = false;

        doNothing().when(userService).updateAccountEnabledStatus(userId, enabledStatus);

        System.out.println("🎭 Disabling user account: " + userId);

        // Act & Assert
        mockMvc.perform(
                        put("/api/admin/update-enabled-status")
                                .param("userId", userId.toString())
                                .param("enabled", String.valueOf(enabledStatus))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Account enabled status updated"));

        System.out.println("✅ Account enabled status updated");

        verify(userService, times(1)).updateAccountEnabledStatus(userId, enabledStatus);
    }

    /**
     * TEST 10: Update credentials expiry status - SUCCESS
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateCredentialsExpiryStatus_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        boolean expireStatus = true;

        doNothing().when(userService).updateCredentialsExpiryStatus(userId, expireStatus);

        System.out.println("🎭 Expiring user credentials: " + userId);

        // Act & Assert
        mockMvc.perform(
                        put("/api/admin/update-credentials-expiry-status")
                                .param("userId", userId.toString())
                                .param("expire", String.valueOf(expireStatus))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Credentials expiry status updated"));

        System.out.println("✅ Credentials expiry status updated");

        verify(userService, times(1)).updateCredentialsExpiryStatus(userId, expireStatus);
    }

    /**
     * TEST 11: Update password - SUCCESS
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updatePassword_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        String newPassword = "newPassword123";

        doNothing().when(userService).updatePassword(userId, newPassword);

        System.out.println("🎭 Updating user password");

        // Act & Assert
        mockMvc.perform(
                        put("/api/admin/update-password")
                                .param("userId", userId.toString())
                                .param("password", newPassword)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated"));

        System.out.println("✅ Password updated successfully");

        verify(userService, times(1)).updatePassword(userId, newPassword);
    }

    /**
     * TEST 12: Update password - Service throws exception
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updatePassword_ServiceThrowsException_ReturnsBadRequest() throws Exception {
        // Arrange
        Long userId = 1L;
        String newPassword = "newPassword123";

        doThrow(new RuntimeException("Password update failed"))
                .when(userService).updatePassword(userId, newPassword);

        System.out.println("🎭 Simulating password update failure");

        // Act & Assert
        mockMvc.perform(
                        put("/api/admin/update-password")
                                .param("userId", userId.toString())
                                .param("password", newPassword)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password update failed"));

        System.out.println("✅ Password update error handled correctly");

        verify(userService, times(1)).updatePassword(userId, newPassword);
    }

    /**
     * TEST 13: Multiple operations - User role ALWAYS denied
     */
    @Test
    @WithMockUser(username = "regularuser", roles = "USER")
    void adminOperations_UserRole_AllDenied() throws Exception {
        // Test multiple endpoints with USER role - all should be forbidden

        System.out.println("🔒 Testing multiple admin operations with USER role");

        // Update role - FORBIDDEN
        mockMvc.perform(put("/api/admin/update-role")
                        .param("userId", "1")
                        .param("roleName", "ROLE_ADMIN"))
                .andExpect(status().isForbidden());

        // Get user - FORBIDDEN
        mockMvc.perform(get("/api/admin/user/1"))
                .andExpect(status().isForbidden());

        // Update lock status - FORBIDDEN
        mockMvc.perform(put("/api/admin/update-lock-status")
                        .param("userId", "1")
                        .param("lock", "true"))
                .andExpect(status().isForbidden());

        // Get roles - FORBIDDEN
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isForbidden());

        System.out.println("✅ All admin operations correctly denied for USER role");

        // Verify NO service methods were called
        verify(userService, never()).updateUserRole(anyLong(), anyString());
        verify(userService, never()).getUserById(anyLong());
        verify(userService, never()).updateAccountLockStatus(anyLong(), anyBoolean());
        verify(userService, never()).getAllRoles();
    }
}

/**
 * 🎓 NEW CONCEPTS YOU LEARNED:
 * <p>
 * 1. ✅ Role-Based Access Control (RBAC) testing
 * 2. ✅ @WithMockUser with different roles (ADMIN vs USER)
 * 3. ✅ Testing 403 Forbidden responses
 * 4. ✅ Testing 401 Unauthorized responses
 * 5. ✅ Multiple PUT operations with query params
 * 6. ✅ Error handling in controllers (try-catch blocks)
 * 7. ✅ Security testing (ensuring unauthorized access is blocked)
 * 8. ✅ doThrow() for testing exceptions
 * 9. ✅ Testing sensitive operations (password updates, account locking)
 * <p>
 * This is SECURITY TESTING - super important! 🔐
 */