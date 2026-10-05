package com.medguide.modules.auth.dto;

import com.medguide.modules.user.domain.Role;
import com.medguide.modules.user.domain.User;
import com.medguide.modules.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Safe user profile representation")
public record UserResponse(
        @Schema(description = "Unique user identifier", example = "1")
        Long id,

        @Schema(description = "User email address", example = "patient@medguide.local")
        String email,

        @Schema(description = "Assigned user role", example = "PATIENT")
        Role role,

        @Schema(description = "Account status", example = "ACTIVE")
        UserStatus status,

        @Schema(description = "Preferred language code", example = "en")
        String preferredLanguage,

        @Schema(description = "Account creation timestamp")
        Instant createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getPreferredLanguage(),
                user.getCreatedAt()
        );
    }
}
