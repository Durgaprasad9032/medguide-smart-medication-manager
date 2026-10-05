package com.medguide.security;

import com.medguide.common.exception.TokenReuseException;
import com.medguide.modules.auth.domain.RefreshToken;
import com.medguide.modules.auth.dto.AuthResponse;
import com.medguide.modules.auth.dto.RefreshTokenRequest;
import com.medguide.modules.auth.dto.RegisterRequest;
import com.medguide.modules.auth.repository.RefreshTokenRepository;
import com.medguide.modules.auth.service.AuthService;
import com.medguide.modules.user.domain.Role;
import com.medguide.modules.user.domain.User;
import com.medguide.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RefreshTokenLifecycleTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setup() {
        String uniqueEmail = "lifecycle_" + UUID.randomUUID() + "@medguide.local";
        authService.register(new RegisterRequest(uniqueEmail, "ValidPass123!", Role.PATIENT, "en"));
        testUser = userRepository.findByEmail(uniqueEmail).orElseThrow();
    }

    @Test
    @DisplayName("Rotate refresh token successfully and detect token reuse on old token")
    void shouldRotateTokenAndDetectReuse() {
        String rawToken1 = jwtService.generateSecureRefreshToken();
        String hash1 = jwtService.hashToken(rawToken1);
        RefreshToken token1 = new RefreshToken(testUser, hash1, Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(token1);

        // 1. Rotate token using valid refresh token
        AuthResponse response = authService.refresh(new RefreshTokenRequest(rawToken1));
        assertThat(response).isNotNull();
        assertThat(response.refreshToken()).isNotEqualTo(rawToken1);

        // Verify old token was revoked and replacement hash linked
        RefreshToken oldStoredToken = refreshTokenRepository.findByTokenHash(hash1).orElseThrow();
        assertThat(oldStoredToken.isRevoked()).isTrue();
        assertThat(oldStoredToken.getReplacedByTokenHash()).isNotNull();

        // 2. Presenting the old revoked token again must trigger TokenReuseException
        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(rawToken1)))
                .isInstanceOf(TokenReuseException.class);
    }
}
