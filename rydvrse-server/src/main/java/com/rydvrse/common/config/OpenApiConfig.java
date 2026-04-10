package com.rydvrse.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rydvrseOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Rydvrse API")
                .description("Rydvrse MVP backend API")
                .version("v1"));
    }
}
