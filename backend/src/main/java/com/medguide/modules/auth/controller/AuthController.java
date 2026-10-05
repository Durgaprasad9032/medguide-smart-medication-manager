package com.medguide.modules.auth.controller;

import com.medguide.common.response.ApiResponse;
import com.medguide.modules.auth.dto.AuthResponse;
import com.medguide.modules.auth.dto.LoginRequest;
import com.medguide.modules.auth.dto.RefreshTokenRequest;
import com.medguide.modules.auth.dto.RegisterRequest;
import com.medguide.modules.auth.dto.UserResponse;
import com.medguide.modules.auth.service.AuthService;
import com.medguide.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication and authorization REST controller.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration, login, JWT token refresh, and session management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new PATIENT or DOCTOR user account")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials", description = "Validates credentials and returns JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT access token", description = "Rotates refresh token and issues a new access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke user session", description = "Revokes the active refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null) {
            authService.logout(request);
        }
        return ResponseEntity.ok(ApiResponse.success("Successfully logged out", null));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get current authenticated user profile", description = "Returns safe profile details of the authenticated caller")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = authService.getCurrentUser(principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =========================================================================
    // RBAC Security Verification Endpoints
    // (Used exclusively to verify role-based authorization guards)
    // =========================================================================

    @GetMapping("/test/patient")
    @PreAuthorize("hasRole('PATIENT')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "RBAC test endpoint for PATIENT role")
    public ResponseEntity<ApiResponse<Map<String, String>>> testPatientRole() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("access", "GRANTED", "role", "PATIENT")));
    }

    @GetMapping("/test/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "RBAC test endpoint for DOCTOR role")
    public ResponseEntity<ApiResponse<Map<String, String>>> testDoctorRole() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("access", "GRANTED", "role", "DOCTOR")));
    }

    @GetMapping("/test/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "RBAC test endpoint for ADMIN role")
    public ResponseEntity<ApiResponse<Map<String, String>>> testAdminRole() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("access", "GRANTED", "role", "ADMIN")));
    }
}
