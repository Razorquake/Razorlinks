package com.razorquake.razorlinks.controller;

import com.razorquake.razorlinks.dtos.LoginRequest;
import com.razorquake.razorlinks.dtos.PasswordResetRequest;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;


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

    @MockitoBean
    private com.razorquake.razorlinks.config.RateLimitConfig rateLimitConfig;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

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

    @Test
    void verifyEmail_ValidToken_ReturnsJwtResponse() throws Exception {
        JwtAuthenticationResponse jwtResponse = new JwtAuthenticationResponse(
                "verified-jwt-token",
                List.of("ROLE_USER")
        );

        when(emailVerificationService.verifyEmail("token-123")).thenReturn(jwtResponse);

        mockMvc.perform(
                        get("/api/auth/public/verify-email")
                                .param("token", "token-123")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully! You are now logged in."))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.token").value("verified-jwt-token"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(emailVerificationService).verifyEmail("token-123");
    }

    @Test
    void resendVerificationEmail_ValidRequest_ReturnsOk() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");

        mockMvc.perform(
                        post("/api/auth/public/resend-verification")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification email sent successfully. Please check your email."))
                .andExpect(jsonPath("$.status").value(true));

        verify(emailVerificationService).resendVerificationEmail("testuser");
    }

    @Test
    void getUserDetails_AuthenticatedUser_ReturnsInfo() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        mockMvc.perform(
                        get("/api/auth/user")
                                .with(user("testuser").roles("USER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(userService).findByUsername("testuser");
    }

    @Test
    void getUsername_AuthenticatedUser_ReturnsName() throws Exception {
        mockMvc.perform(
                        get("/api/auth/username")
                                .with(user("testuser"))
                )
                .andExpect(status().isOk())
                .andExpect(content().string("testuser"));
    }

    @Test
    void resetPassword_ValidRequest_ReturnsOk() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("token", "reset-token");
        request.put("newPassword", "newpassword123");

        mockMvc.perform(
                        post("/api/auth/public/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully"))
                .andExpect(jsonPath("$.status").value(true));

        verify(passwordResetService).resetPassword(any(PasswordResetRequest.class));
    }

    @Test
    void disable2FA_AuthenticatedUser_Disables2FA() throws Exception {
        when(userService.loggedInUser()).thenReturn(testUser);

        mockMvc.perform(
                        post("/api/auth/disable-2fa")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("2FA disabled"));

        verify(userService).disable2FA(testUser.getId());
    }

    @Test
    void verify2FA_ValidCode_ReturnsOkAndEnables() throws Exception {
        when(userService.loggedInUser()).thenReturn(testUser);
        when(userService.validate2FACode(testUser.getId(), 123456)).thenReturn(true);

        mockMvc.perform(
                        post("/api/auth/verify-2fa")
                                .param("code", "123456")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("2FA Verified"));

        verify(userService).enable2FA(testUser.getId());
    }

    @Test
    void verify2FA_InvalidCode_ReturnsUnauthorized() throws Exception {
        when(userService.loggedInUser()).thenReturn(testUser);
        when(userService.validate2FACode(testUser.getId(), 111111)).thenReturn(false);

        mockMvc.perform(
                        post("/api/auth/verify-2fa")
                                .param("code", "111111")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid 2FA Code"));

        verify(userService, never()).enable2FA(any());
    }

    @Test
    void get2FAStatus_UserFound_ReturnsStatus() throws Exception {
        testUser.setTwoFactorEnabled(true);
        when(userService.loggedInUser()).thenReturn(testUser);

        mockMvc.perform(
                        get("/api/auth/user/2fa-status")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is2faEnabled").value(true));
    }

    @Test
    void get2FAStatus_UserMissing_Returns404() throws Exception {
        when(userService.loggedInUser()).thenReturn(null);

        mockMvc.perform(
                        get("/api/auth/user/2fa-status")
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    void verify2FALogin_ValidCode_ReturnsOk() throws Exception {
        when(jwtUtils.getUserNameFromJwtToken("jwt-token")).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(userService.validate2FACode(testUser.getId(), 654321)).thenReturn(true);

        mockMvc.perform(
                        post("/api/auth/public/verify-2fa-login")
                                .param("code", "654321")
                                .param("jwtToken", "jwt-token")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("2FA Verified"));

        verify(jwtUtils).getUserNameFromJwtToken(eq("jwt-token"));
        verify(userService).findByUsername("testuser");
    }

    @Test
    void verify2FALogin_InvalidCode_ReturnsUnauthorized() throws Exception {
        when(jwtUtils.getUserNameFromJwtToken("jwt-token")).thenReturn("testuser");
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(userService.validate2FACode(testUser.getId(), 222222)).thenReturn(false);

        mockMvc.perform(
                        post("/api/auth/public/verify-2fa-login")
                                .param("code", "222222")
                                .param("jwtToken", "jwt-token")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid 2FA Code"));

        verify(userService, never()).enable2FA(any());
    }

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }
}
