package com.razorquake.razorlinks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorquake.razorlinks.dtos.LoginRequest;
import com.razorquake.razorlinks.dtos.RegisterRequest;
import com.razorquake.razorlinks.dtos.UserDTO;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.security.jwt.JwtAuthenticationResponse;
import com.razorquake.razorlinks.security.jwt.JwtUtils;
import com.razorquake.razorlinks.service.EmailVerificationService;
import com.razorquake.razorlinks.service.PasswordResetService;
import com.razorquake.razorlinks.service.TotpService;
import com.razorquake.razorlinks.service.UserService;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private TotpService totpService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);

        testUserDTO = new UserDTO(
                1L,
                "testuser",
                "test@example.com",
                true,
                true,
                true,
                true,
                null,
                null,
                null,
                false,
                "SIGNUP",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        System.out.println("\n🌐 === NEW CONTROLLER TEST STARTING ===");
    }

    @Test
    void registerUser_ValidRequest_ReturnsOkWithMessage() throws Exception {
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("message", "User registered successfully. Please check your email to verify your account.");
        expectedResponse.put("email", registerRequest.getEmail());
        expectedResponse.put("status", true);

        when(userService.registerUser(any(RegisterRequest.class)))
                .thenReturn(expectedResponse);

        System.out.println("🎭 Mocked userService.registerUser()");

        mockMvc.perform(
                        post("/api/auth/public/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User registered successfully. Please check your email to verify your account."))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value(true));

        System.out.println("✅ Registration endpoint returned 200 OK");
    }

    @Test
    void registerUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        registerRequest.setEmail("not-an-email");

        System.out.println("📤 Sending invalid email: " + registerRequest.getEmail());

        mockMvc.perform(
                        post("/api/auth/public/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        System.out.println("✅ Invalid email correctly rejected with 400");
    }

    @Test
    void registerUser_ShortPassword_ReturnsBadRequest() throws Exception {
        registerRequest.setPassword("123");

        System.out.println("📤 Sending short password: " + registerRequest.getPassword());

        mockMvc.perform(
                        post("/api/auth/public/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        System.out.println("✅ Short password correctly rejected");
    }

    @Test
    void loginUser_ValidCredentials_ReturnsTokenAndRoles() throws Exception {
        JwtAuthenticationResponse jwtResponse = new JwtAuthenticationResponse(
                "fake-jwt-token-12345",
                List.of("ROLE_USER")
        );

        when(userService.authenticateUser(any(LoginRequest.class)))
                .thenReturn(jwtResponse);

        System.out.println("🎭 Mocked login to return JWT token");

        mockMvc.perform(
                        post("/api/auth/public/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token-12345"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        System.out.println("✅ Login successful, JWT token returned");
    }

    @Test
    void loginUser_EmptyPassword_ReturnsBadRequest() throws Exception {
        loginRequest.setPassword("");

        System.out.println("📤 Attempting login with empty password");

        mockMvc.perform(
                        post("/api/auth/public/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        System.out.println("✅ Empty password correctly rejected");
    }

    @Test
    void forgotPassword_ValidEmail_ReturnsSuccessMessage() throws Exception {
        String email = "test@example.com";

        System.out.println("📤 Requesting password reset for: " + email);

        mockMvc.perform(
                        post("/api/auth/public/forgot-password")
                                .param("email", email)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent to " + email))
                .andExpect(jsonPath("$.status").value(true));

        System.out.println("✅ Password reset email request successful");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void enable2FA_AuthenticatedUser_ReturnsQRCodeUrl() throws Exception {
        String qrCodeUrl = "https://example.com/qr-code";
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder("SECRETKEY").build();

        when(userService.loggedInUser()).thenReturn(testUser);
        when(userService.generate2FASecret(testUser.getId())).thenReturn(key);
        when(userService.getUserById(testUser.getId())).thenReturn(testUserDTO);
        when(totpService.getQrCodeUrl(any(), any()))
                .thenReturn(qrCodeUrl);

        System.out.println("🔐 Simulating authenticated user");

        mockMvc.perform(
                        post("/api/auth/enable-2fa")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(qrCodeUrl));

        System.out.println("✅ 2FA enabled, QR code URL returned");
    }

    @Test
    void registerUser_NullUsername_ReturnsBadRequest() throws Exception {
        registerRequest.setUsername(null);

        mockMvc.perform(
                        post("/api/auth/public/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_ShortUsername_ReturnsBadRequest() throws Exception {
        registerRequest.setUsername("ab");  // Min is 3

        mockMvc.perform(
                        post("/api/auth/public/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUser_NullUsername_ReturnsBadRequest() throws Exception {
        loginRequest.setUsername(null);

        mockMvc.perform(
                        post("/api/auth/public/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPassword_EmptyEmail_ReturnsSuccessAnyway() throws Exception {
        mockMvc.perform(
                        post("/api/auth/public/forgot-password")
                                .param("email", "")
                )
                .andExpect(status().isOk());
    }

    @Test
    void enable2FA_NotAuthenticated_Returns401() throws Exception {
        // Mock loggedInUser to return null (simulating no authenticated user)
        when(userService.loggedInUser()).thenReturn(null);
        mockMvc.perform(
                        post("/api/auth/enable-2fa")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not authenticated"))
                .andExpect(jsonPath("$.status").value(false));
    }

}
