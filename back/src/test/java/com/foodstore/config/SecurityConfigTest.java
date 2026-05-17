package com.foodstore.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodstore.security.JwtProvider;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfigTest.TestController.class)
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @RestController
    static class TestController {

        @GetMapping("/test/secure")
        public Map<String, Object> secure(Authentication authentication) {
            return Map.of(
                    "name", authentication.getName(),
                    "authorities", authentication.getAuthorities()
                            .stream()
                            .map(grantedAuthority -> grantedAuthority.getAuthority())
                            .collect(Collectors.toSet())
            );
        }
    }

    @Test
    void passwordEncoder_ShouldUseBCryptWithStrength10() {
        String rawPassword = "testPassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).startsWith("$2a$10$");
    }

    @Test
    void passwordEncoder_ShouldMatchCorrectPassword() {
        String rawPassword = "testPassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
    }

    @Test
    void passwordEncoder_ShouldNotMatchWrongPassword() {
        String rawPassword = "testPassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matches("wrongPassword", encoded)).isFalse();
    }

    @Test
    void corsPreflight_ShouldReturnAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    void corsPreflight_ShouldRejectDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://evil-site.com")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void protectedRoute_ShouldAuthenticateWithValidBearerToken() throws Exception {
        String token = jwtProvider.generateToken(1L, "user@test.com", "ROLE_USER");

        mockMvc.perform(get("/test/secure")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("1"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));
    }

    @Test
    void protectedRoute_ShouldRejectInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/test/secure")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}
