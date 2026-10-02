package com.medguide.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 configuration for MedGuide REST API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI medGuideOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MedGuide API")
                        .version("v1")
                        .description("Multilingual Smart Medication Manager REST API")
                        .contact(new Contact()
                                .name("MedGuide Engineering Team")
                                .email("support@medguide.local"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://medguide.local")));
    }
}
