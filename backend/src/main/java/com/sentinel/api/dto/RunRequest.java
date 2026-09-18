package com.sentinel.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for launching a simulation run.
 */
public record RunRequest(
        @NotBlank(message = "scenarioId is required") String scenarioId
) {
}
