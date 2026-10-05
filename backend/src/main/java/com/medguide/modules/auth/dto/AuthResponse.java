package com.medguide.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication token response payload")
public record AuthResponse(
        @Schema(description = "Signed JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Authorization header scheme type", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token validity duration in milliseconds", example = "900000")
        long expiresIn,

        @Schema(description = "Cryptographically secure refresh token string", example = "0c85b19e-5e74-4b53-...")
        String refreshToken,

        @Schema(description = "Authenticated user profile")
        UserResponse user
) {
    public static AuthResponse of(String accessToken, long expiresIn, String refreshToken, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresIn, refreshToken, user);
    }
}
