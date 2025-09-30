package com.razorquake.razorlinks.security.config;

import com.razorquake.razorlinks.models.AppRole;
import com.razorquake.razorlinks.models.Role;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.RoleRepository;
import com.razorquake.razorlinks.repository.UserRepository;
import com.razorquake.razorlinks.security.jwt.JwtUtils;
import com.razorquake.razorlinks.security.service.UserDetailsImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Value("${frontend.url}")
    private String frontendUrl;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        String username;
        String idAttributeKey;

        DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = principal.getAttributes();

        String email = attributes.getOrDefault("email", "").toString();
        String provider = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

        if ("github".equals(provider)) {
            username = attributes.getOrDefault("login", "").toString();
            idAttributeKey = "id";
        } else if ("google".equals(provider)) {
            username = email.split("@")[0];
            idAttributeKey = "sub";
        } else {
            // Handle other providers or throw an error
            throw new ServletException("Unsupported OAuth2 provider: " + provider);
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    Optional<Role> userRole = roleRepository.findByRoleName(AppRole.ROLE_USER); // Fetch an existing role
                    if (userRole.isPresent()) {
                        newUser.setRole(userRole.get()); // Set existing role
                    } else {
                        // Handle the case where the role is not found
                        throw new RuntimeException("Default role not found");
                    }
                    newUser.setEmail(email);
                    newUser.setUsername(username);
                    newUser.setSignUpMethod(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
                    newUser.setEnabled(true);
                    return userRepository.save(newUser);
                });

        DefaultOAuth2User oauthUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name())),
                attributes,
                idAttributeKey
        );
        Authentication securityAuth = new OAuth2AuthenticationToken(
                oauthUser,
                List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name())),
                oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
        );
        SecurityContextHolder.getContext().setAuthentication(securityAuth);
        this.setAlwaysUseDefaultTargetUrl(true);

        // Create a UserDetailsImpl instance
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // Generate JWT token
        String jwtToken = jwtUtils.generateToken(userDetails);

        // Redirect to the frontend with the JWT token
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("token", jwtToken)
                .build().toUriString();

        // Use a clear redirect strategy instead of setting default URLs
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
