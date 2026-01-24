package com.razorquake.razorlinks.security.config;

import com.razorquake.razorlinks.models.AppRole;
import com.razorquake.razorlinks.models.Role;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.RoleRepository;
import com.razorquake.razorlinks.repository.UserRepository;
import com.razorquake.razorlinks.security.jwt.JwtUtils;
import com.razorquake.razorlinks.security.service.UserDetailsImpl;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OAuth2LoginSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(successHandler, "frontendUrl", "http://frontend");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void onAuthenticationSuccess_GithubExistingUser_RedirectsWithToken() throws Exception {
        Role role = new Role(AppRole.ROLE_USER);
        User user = new User("githubuser", "test@example.com");
        user.setRole(role);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(any(UserDetailsImpl.class))).thenReturn("jwt-token");

        OAuth2AuthenticationToken authentication = buildAuthenticationToken(
                "github",
                Map.of("email", "test@example.com", "login", "githubuser", "id", "123"),
                "id"
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://frontend/oauth2/redirect?token=jwt-token");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void onAuthenticationSuccess_GoogleNewUser_CreatesUserAndRedirects() throws Exception {
        Role role = new Role(AppRole.ROLE_USER);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(AppRole.ROLE_USER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtils.generateToken(any(UserDetailsImpl.class))).thenReturn("jwt-token");

        OAuth2AuthenticationToken authentication = buildAuthenticationToken(
                "google",
                Map.of("email", "test@example.com", "sub", "sub-123"),
                "sub"
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getUsername()).isEqualTo("test");
        assertThat(savedUser.getSignUpMethod()).isEqualTo("google");
        assertThat(savedUser.isEnabled()).isTrue();

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://frontend/oauth2/redirect?token=jwt-token");
    }

    @Test
    void onAuthenticationSuccess_UnsupportedProvider_ThrowsException() {
        OAuth2AuthenticationToken authentication = buildAuthenticationToken(
                "facebook",
                Map.of("email", "test@example.com", "id", "123"),
                "id"
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> successHandler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("Unsupported OAuth2 provider");
    }

    private OAuth2AuthenticationToken buildAuthenticationToken(String provider,
                                                               Map<String, Object> attributes,
                                                               String nameAttributeKey) {
        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                nameAttributeKey
        );
        return new OAuth2AuthenticationToken(principal, principal.getAuthorities(), provider);
    }
}
