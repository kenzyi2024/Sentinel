package com.sentinel.api.dto;

import java.util.List;

/**
 * Metadata describing the running engine — surfaced by the API so the UI can show what the system
 * is (and prove it is deterministic and local, with no external model).
 */
public record EngineMeta(
        String name,
        String version,
        boolean deterministic,
        boolean usesExternalModel,
        int riskScorers,
        int scenarios,
        long totalRuns,
        long totalEvents,
        List<String> capabilities,
        List<String> actionTypes
) {
}
