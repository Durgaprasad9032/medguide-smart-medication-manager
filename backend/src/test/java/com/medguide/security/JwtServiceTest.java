package com.medguide.security;

import com.medguide.modules.user.domain.Role;
import com.medguide.modules.user.domain.User;
import com.medguide.modules.user.domain.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Generate and validate valid JWT access token")
    void shouldGenerateAndValidateToken() {
        User user = new User("test@medguide.local", "hashedPass", Role.PATIENT, UserStatus.ACTIVE, "en");
        user.setId(100L);

        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateAccessToken(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(100L);
        assertThat(jwtService.extractEmail(token)).isEqualTo("test@medguide.local");
        assertThat(jwtService.extractRole(token)).isEqualTo(Role.PATIENT);
    }

    @Test
    @DisplayName("Reject malformed or tampered token")
    void shouldRejectInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalidPayload.tamperedSignature";
        assertThat(jwtService.validateAccessToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("Generate secure random refresh token and compute consistent SHA-256 hash")
    void shouldGenerateAndHashRefreshToken() {
        String rawToken1 = jwtService.generateSecureRefreshToken();
        String rawToken2 = jwtService.generateSecureRefreshToken();

        assertThat(rawToken1).isNotBlank();
        assertThat(rawToken2).isNotBlank();
        assertThat(rawToken1).isNotEqualTo(rawToken2);

        String hash1 = jwtService.hashToken(rawToken1);
        String hash1Repeat = jwtService.hashToken(rawToken1);

        assertThat(hash1).hasSize(64);
        assertThat(hash1).isEqualTo(hash1Repeat);
    }
}
