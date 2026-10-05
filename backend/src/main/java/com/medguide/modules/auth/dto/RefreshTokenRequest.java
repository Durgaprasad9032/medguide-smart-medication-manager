package com.medguide.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token request payload")
public record RefreshTokenRequest(
        @Schema(description = "Cryptographically secure refresh token string", example = "dGhpcy1pcy1hLXJlZnJlc2gtdG9rZW4...")
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
