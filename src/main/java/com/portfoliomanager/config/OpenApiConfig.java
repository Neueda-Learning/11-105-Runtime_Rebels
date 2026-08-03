package com.portfoliomanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI portfolioManagerOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Portfolio Manager API")
                .description("REST API for tracking stocks, ETFs, fixed deposits and cash across multiple "
                        + "countries and currencies, with a consolidated dashboard, realized/unrealized P&L, "
                        + "performance history and wealth milestones.")
                .version("v0.1.0")
                .contact(new Contact().name("Portfolio Manager Team")));
    }
}