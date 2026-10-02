package com.medguide.modules.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Foundational health check controller.
 * Accessible at GET /api/v1/health.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health Check", description = "System liveness and service status verification")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Check backend service health", description = "Returns service status and identifier")
    public ResponseEntity<HealthResponse> checkHealth() {
        return ResponseEntity.ok(HealthResponse.up());
    }
}
