package com.medguide.modules.auth.service;

import com.medguide.common.exception.AccountStatusException;
import com.medguide.common.exception.ApiException;
import com.medguide.common.exception.DuplicateEmailException;
import com.medguide.common.exception.ResourceNotFoundException;
import com.medguide.common.exception.TokenReuseException;
import com.medguide.modules.auth.domain.RefreshToken;
import com.medguide.modules.auth.dto.AuthResponse;
import com.medguide.modules.auth.dto.LoginRequest;
import com.medguide.modules.auth.dto.RefreshTokenRequest;
import com.medguide.modules.auth.dto.RegisterRequest;
import com.medguide.modules.auth.dto.UserResponse;
import com.medguide.modules.auth.repository.RefreshTokenRepository;
import com.medguide.modules.user.domain.Role;
import com.medguide.modules.user.domain.User;
import com.medguide.modules.user.domain.UserStatus;
import com.medguide.modules.user.repository.UserRepository;
import com.medguide.security.JwtService;
import com.medguide.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Core authentication service handling registration, credentials verification,
 * JWT token issuance, refresh-token rotation, token reuse detection, and session revocation.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        // Security rule: Prevent public registration as privileged ADMIN
        if (request.role() == Role.ADMIN) {
            log.warn("Blocked public attempt to register ADMIN account with email: {}", normalizedEmail);
            throw new ApiException("Public registration as ADMIN is not permitted", HttpStatus.FORBIDDEN);
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        // Doctor accounts initialize in PENDING verification status; patients initialize as ACTIVE
        UserStatus initialStatus = (request.role() == Role.DOCTOR) ? UserStatus.PENDING : UserStatus.ACTIVE;
        String language = (request.preferredLanguage() != null && !request.preferredLanguage().isBlank())
                ? request.preferredLanguage().trim().toLowerCase()
                : "en";

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.password()),
                request.role(),
                initialStatus,
                language
        );

        try {
            User savedUser = userRepository.save(user);
            log.info("Registered new user [ID: {}, Role: {}, Status: {}]", savedUser.getId(), savedUser.getRole(), savedUser.getStatus());
            return UserResponse.fromEntity(savedUser);
        } catch (DataIntegrityViolationException e) {
            log.warn("Database constraint caught duplicate email during registration: {}", normalizedEmail);
            throw new DuplicateEmailException(normalizedEmail);
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Enforce account lifecycle status policy
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DEACTIVATED) {
            log.warn("Authentication rejected for user [{}] due to inactive status [{}]", normalizedEmail, user.getStatus());
            throw new AccountStatusException(user.getStatus());
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed authentication attempt for email: {}", normalizedEmail);
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = jwtService.generateSecureRefreshToken();
        String tokenHash = jwtService.hashToken(rawRefreshToken);

        Instant expiresAt = Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs());
        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        log.info("User logged in successfully [ID: {}, Role: {}]", user.getId(), user.getRole());
        return AuthResponse.of(
                accessToken,
                jwtService.getAccessTokenExpirationMs(),
                rawRefreshToken,
                UserResponse.fromEntity(user)
        );
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String rawToken = request.refreshToken().trim();
        String incomingHash = jwtService.hashToken(rawToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(incomingHash)
                .orElseThrow(() -> new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        User user = storedToken.getUser();

        // Token Reuse Detection: If a revoked token is presented again, compromise is suspected
        if (storedToken.isRevoked()) {
            log.warn("SECURITY ALERT: Revoked refresh token reuse detected for User ID: {}. Revoking all active sessions.", user.getId());
            refreshTokenRepository.revokeAllByUserId(user.getId());
            throw new TokenReuseException("Revoked refresh token reuse detected. All active sessions have been terminated for security.");
        }

        // Expiration check
        if (storedToken.isExpired()) {
            log.warn("Expired refresh token presented for User ID: {}", user.getId());
            throw new ApiException("Refresh token has expired. Please log in again.", HttpStatus.UNAUTHORIZED);
        }

        // Account status check
        if (!user.getStatus().canAuthenticate()) {
            throw new AccountStatusException(user.getStatus());
        }

        // Rotate: Generate new refresh token and link old token
        String newRawRefreshToken = jwtService.generateSecureRefreshToken();
        String newHash = jwtService.hashToken(newRawRefreshToken);

        storedToken.revoke(newHash);
        refreshTokenRepository.save(storedToken);

        Instant newExpiry = Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs());
        RefreshToken newRefreshToken = new RefreshToken(user, newHash, newExpiry);
        refreshTokenRepository.save(newRefreshToken);

        String newAccessToken = jwtService.generateAccessToken(user);
        log.info("Refreshed access token successfully for User ID: {}", user.getId());

        return AuthResponse.of(
                newAccessToken,
                jwtService.getAccessTokenExpirationMs(),
                newRawRefreshToken,
                UserResponse.fromEntity(user)
        );
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
            return;
        }

        String rawToken = request.refreshToken().trim();
        String tokenHash = jwtService.hashToken(rawToken);

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.revoke();
                refreshTokenRepository.save(token);
                log.info("Revoked refresh token for User ID: {}", token.getUser().getId());
            }
        });
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new ApiException("No authenticated principal found", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        return UserResponse.fromEntity(user);
    }
}
