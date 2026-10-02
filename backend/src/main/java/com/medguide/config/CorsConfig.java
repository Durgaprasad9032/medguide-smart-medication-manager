package com.medguide.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Cross-Origin Resource Sharing (CORS) configuration foundation.
 * Configured dynamically through application environment properties for Flutter Web and mobile clients.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080,http://localhost:5000,http://127.0.0.1:3000}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age-secs:3600}")
    private long maxAgeSecs;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(Arrays.stream(allowedMethods.split(",")).map(String::trim).toList());
        config.setAllowedHeaders(Arrays.stream(allowedHeaders.split(",")).map(String::trim).toList());
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(maxAgeSecs);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
