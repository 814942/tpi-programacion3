package com.foodstore.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class JwtProviderTest {

    @Nested
    @SpringBootTest(classes = JwtProvider.class)
    @TestPropertySource(properties = {
            "app.jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-hs256",
            "app.jwt.expiration=86400000",
            "app.jwt.secret.is-base64=false"
    })
    class ValidTokenTests {

        @Autowired
        private JwtProvider jwtProvider;

        private String token;

        @BeforeEach
        void setUp() {
            token = jwtProvider.generateToken(1L, "user@test.com", "ROLE_USER");
        }

        @Test
        void generateToken_ShouldReturnValidJwtStructure() {
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        void validateToken_ShouldReturnTrueForValidToken() {
            assertThat(jwtProvider.validateToken(token)).isTrue();
        }

        @Test
        void getUserIdFromToken_ShouldExtractUserId() {
            Long userId = jwtProvider.getUserIdFromToken(token);
            assertThat(userId).isEqualTo(1L);
        }

        @Test
        void getRoleFromToken_ShouldExtractRole() {
            String role = jwtProvider.getRoleFromToken(token);
            assertThat(role).isEqualTo("ROLE_USER");
        }

        @Test
        void getEmailFromToken_ShouldExtractEmail() {
            String email = jwtProvider.getEmailFromToken(token);
            assertThat(email).isEqualTo("user@test.com");
        }

        @Test
        void validateToken_ShouldReturnFalseForTamperedToken() {
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";
            assertThat(jwtProvider.validateToken(tampered)).isFalse();
        }

        @Test
        void validateToken_ShouldReturnFalseForMalformedToken() {
            assertThat(jwtProvider.validateToken("not-a-jwt-token")).isFalse();
        }

        @Test
        void validateToken_ShouldReturnFalseForEmptyToken() {
            assertThat(jwtProvider.validateToken("")).isFalse();
        }
    }

    @Nested
    @SpringBootTest(classes = JwtProvider.class)
    @TestPropertySource(properties = {
            "app.jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-hs256",
            "app.jwt.expiration=-100000",
            "app.jwt.secret.is-base64=false"
    })
    class ExpiredTokenTests {

        @Autowired
        private JwtProvider jwtProvider;

        @Test
        void validateToken_ShouldReturnFalseForExpiredToken() {
            String token = jwtProvider.generateToken(1L, "user@test.com", "ROLE_USER");
            assertThat(jwtProvider.validateToken(token)).isFalse();
        }
    }
}
