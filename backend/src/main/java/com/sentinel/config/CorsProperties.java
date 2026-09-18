package com.sentinel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Allowed CORS origins for the API (bound from {@code sentinel.cors.allowed-origins}). Defaults to
 * the local Vite dev servers so the frontend can call a locally-running backend.
 */
@ConfigurationProperties("sentinel.cors")
public class CorsProperties {

    private List<String> allowedOrigins = List.of("http://localhost:5173", "http://localhost:4173");

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
