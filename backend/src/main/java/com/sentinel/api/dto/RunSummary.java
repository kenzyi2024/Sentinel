package com.sentinel.api.dto;

import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.RiskBand;
import com.sentinel.domain.enums.RunStatus;

import java.time.Instant;

/**
 * A run as it appears in list views — headline metadata and aggregate counters, without the full
 * event stream or report.
 */
public record RunSummary(
        String id,
        String scenarioId,
        String scenarioName,
        String agentName,
        AgentArchetype archetype,
        RunStatus status,
        Instant startedAt,
        Instant finishedAt,
        int totalActions,
        int allowed,
        int blocked,
        int requiresApproval,
        int maxRiskScore,
        RiskBand overallRiskBand
) {
}
