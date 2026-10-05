package com.medguide.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login credentials payload")
public record LoginRequest(
        @Schema(description = "Registered account email address", example = "patient@medguide.local")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address format")
        String email,

        @Schema(description = "Account password", example = "SecurePass123!")
        @NotBlank(message = "Password is required")
        String password
) {
}
