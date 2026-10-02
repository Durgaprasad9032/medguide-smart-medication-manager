package com.medguide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Foundational Configuration.
 * <p>
 * IMPORTANT NOTE FOR DEVELOPMENT (PHASE 1 FOUNDATION):
 * This is a foundational development security configuration ensuring that the application context,
 * health endpoints, Actuator, and Swagger/OpenAPI documentation start and operate smoothly without
 * interference from default Spring Security form-login or basic auth prompts.
 * <p>
 * In PHASE 3, this configuration will be extended with:
 * <ul>
 *   <li>JWT Authentication Filter</li>
 *   <li>JWT Token Provider & Validation</li>
 *   <li>UserDetailsService & BCrypt Password Encoder</li>
 *   <li>Role-based access control (PATIENT, DOCTOR, ADMIN)</li>
 *   <li>Authentication entry point & access denied handler</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Enforce stateless session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure endpoint authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/**",
                                "/error"
                        ).permitAll()

                        // During Phase 1 foundation, allow remaining paths for local verification.
                        // In Phase 3, this will be strictly configured with authenticated() and hasRole(...) guards.
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
