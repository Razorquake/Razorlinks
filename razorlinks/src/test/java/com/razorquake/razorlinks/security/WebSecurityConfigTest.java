package com.razorquake.razorlinks.security;

import com.razorquake.razorlinks.models.AppRole;
import com.razorquake.razorlinks.models.Role;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.RoleRepository;
import com.razorquake.razorlinks.repository.UserRepository;
import com.razorquake.razorlinks.security.config.OAuth2LoginSuccessHandler;
import com.razorquake.razorlinks.security.jwt.AuthEntryPointJwt;
import com.razorquake.razorlinks.security.jwt.JwtAuthenticationFilter;
import com.razorquake.razorlinks.security.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSecurityConfigTest {

    @Mock
    private AuthEntryPointJwt unauthorizedHandler;

    @Mock
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private RateLimitingFilter rateLimitingFilter;

    private WebSecurityConfig webSecurityConfig;

    @BeforeEach
    void setUp() {
        webSecurityConfig = new WebSecurityConfig(
                unauthorizedHandler,
                oAuth2LoginSuccessHandler,
                userDetailsService,
                jwtAuthenticationFilter,
                rateLimitingFilter
        );
        ReflectionTestUtils.setField(webSecurityConfig, "adminEmail", "admin@example.com");
        ReflectionTestUtils.setField(webSecurityConfig, "adminPassword", "adminpass");
    }

    @Test
    void passwordEncoder_ReturnsBCrypt() {
        assertThat(webSecurityConfig.passwordEncoder()).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void authenticationProvider_BuildsProvider() {
        DaoAuthenticationProvider provider = webSecurityConfig.authenticationProvider();

        assertThat(provider).isNotNull();
    }

    @Test
    void authenticationManager_DelegatesToConfiguration() throws Exception {
        AuthenticationConfiguration configuration = mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(configuration.getAuthenticationManager()).thenReturn(manager);

        AuthenticationManager result = webSecurityConfig.authenticationManager(configuration);

        assertThat(result).isSameAs(manager);
    }

    @Test
    void initData_CreatesRolesAndAdminUser() throws Exception {
        RoleRepository roleRepository = mock(RoleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(roleRepository.findByRoleName(AppRole.ROLE_USER)).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(AppRole.ROLE_ADMIN)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("adminpass")).thenReturn("encoded-pass");

        CommandLineRunner runner = webSecurityConfig.initData(roleRepository, userRepository, passwordEncoder);
        runner.run();

        verify(roleRepository).save(argThat(role -> role.getRoleName() == AppRole.ROLE_USER));
        verify(roleRepository).save(argThat(role -> role.getRoleName() == AppRole.ROLE_ADMIN));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("admin");
        assertThat(savedUser.getEmail()).isEqualTo("admin@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-pass");
        assertThat(savedUser.getRole().getRoleName()).isEqualTo(AppRole.ROLE_ADMIN);
        assertThat(savedUser.getSignUpMethod()).isEqualTo("email");
        assertThat(savedUser.isEnabled()).isTrue();
    }

    @Test
    void initData_AdminAlreadyExists_SkipsCreation() throws Exception {
        RoleRepository roleRepository = mock(RoleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(roleRepository.findByRoleName(AppRole.ROLE_USER)).thenReturn(Optional.of(new Role(AppRole.ROLE_USER)));
        when(roleRepository.findByRoleName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(new Role(AppRole.ROLE_ADMIN)));
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        CommandLineRunner runner = webSecurityConfig.initData(roleRepository, userRepository, passwordEncoder);
        runner.run();

        verify(roleRepository, never()).save(any(Role.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
