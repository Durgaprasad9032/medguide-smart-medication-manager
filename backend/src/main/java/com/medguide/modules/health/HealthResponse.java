package com.medguide.modules.health;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Health check response model.
 */
@Schema(description = "Health status of the MedGuide backend service")
public record HealthResponse(
        @Schema(description = "Service status indicator", example = "UP")
        String status,

        @Schema(description = "Service name identifier", example = "MedGuide Backend")
        String service
) {
    public static HealthResponse up() {
        return new HealthResponse("UP", "MedGuide Backend");
    }
}
