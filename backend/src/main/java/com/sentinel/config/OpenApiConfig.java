package com.sentinel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI document that backs the interactive Swagger UI at {@code /swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sentinelOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Sentinel API")
                .version("0.1.0")
                .description("Deterministic, local-only engine for evaluating simulated AI-agent actions: "
                        + "permission checks, policy evaluation, explainable risk scoring, prompt-injection "
                        + "detection, and behavioral anomaly detection. No external LLM or cloud service is used.")
                .license(new License().name("MIT")));
    }
}
