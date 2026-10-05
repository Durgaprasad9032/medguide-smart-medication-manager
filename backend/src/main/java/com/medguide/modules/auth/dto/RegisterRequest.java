package com.medguide.modules.auth.dto;

import com.medguide.modules.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration payload")
public record RegisterRequest(
        @Schema(description = "Unique user email address", example = "patient@medguide.local")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address format")
        String email,

        @Schema(description = "Account password (minimum 8 characters)", example = "SecurePass123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password,

        @Schema(description = "Account role (PATIENT or DOCTOR only for public registration)", example = "PATIENT")
        @NotNull(message = "Role is required")
        Role role,

        @Schema(description = "Preferred language code ('en' or 'te')", example = "en", defaultValue = "en")
        @Pattern(regexp = "^(en|te)$", message = "Preferred language must be 'en' or 'te'")
        String preferredLanguage
) {
}
