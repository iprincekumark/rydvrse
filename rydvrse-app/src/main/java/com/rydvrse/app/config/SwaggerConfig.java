package com.rydvrse.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI / Swagger configuration.
 * Provides interactive API documentation at /api/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI rydvrseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RYDVRSE Platform API")
                        .description("""
                                **RYDVRSE** — On-Demand Driver Marketplace API
                                
                                "Your Car. Our Driver. Your Destination."
                                
                                This API powers the RYDVRSE platform, enabling customers to book 
                                professional drivers for their personal vehicles across India.
                                
                                ## Authentication
                                All protected endpoints require a **Bearer JWT token** in the Authorization header.
                                Use the `/v1/auth/otp/send` and `/v1/auth/otp/verify` endpoints to obtain tokens.
                                
                                ## Rate Limits
                                - Auth endpoints: 5 req/min per IP
                                - Trip creation: 3 req/min per user
                                - Location updates: 60 req/min per driver
                                """)
                        .version(appVersion)
                        .contact(new Contact()
                                .name("RYDVRSE Engineering")
                                .email("engineering@rydvrse.com")
                                .url("https://rydvrse.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://rydvrse.com/terms")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("Local Development"),
                        new Server().url("https://staging-api.rydvrse.com").description("Staging"),
                        new Server().url("https://api.rydvrse.com").description("Production")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from OTP verification")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .tags(List.of(
                        new Tag().name("Auth").description("Authentication & OTP verification"),
                        new Tag().name("Customers").description("Customer profile management"),
                        new Tag().name("Drivers").description("Driver onboarding & management"),
                        new Tag().name("Trips").description("Trip lifecycle management (core)"),
                        new Tag().name("Locations").description("Real-time GPS tracking"),
                        new Tag().name("Pricing").description("Fare estimation & pricing rules"),
                        new Tag().name("Payments").description("Payment processing & history"),
                        new Tag().name("Wallet").description("Wallet & balance management"),
                        new Tag().name("Support").description("Support tickets"),
                        new Tag().name("Safety").description("Emergency SOS & incident reports"),
                        new Tag().name("Admin").description("Admin dashboard & operations")));
    }
}
