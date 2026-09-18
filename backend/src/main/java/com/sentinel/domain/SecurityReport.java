package com.sentinel.domain;

import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.AgentState;
import com.sentinel.domain.enums.RiskBand;
import com.sentinel.domain.enums.RunStatus;

import java.time.Instant;
import java.util.List;

/**
 * The post-run security report. Every field is derived deterministically from the run's persisted
 * events — no external service and no generative model is involved in producing it.
 */
public record SecurityReport(
        String runId,
        String scenarioId,
        String scenarioName,
        String agentName,
        AgentArchetype archetype,
        RunStatus status,
        AgentState finalAgentState,
        int totalActions,
        int allowed,
        int blocked,
        int requiresApproval,
        int warningsRaised,
        int policyViolations,
        int injectionFindings,
        int anomaliesDetected,
        int maxRiskScore,
        double averageRiskScore,
        RiskBand overallRiskBand,
        List<EventSummary> highestRiskEvents,
        List<String> detectedAttackPatterns,
        List<String> behavioralAnomalies,
        List<String> recommendedPolicyChanges,
        Instant generatedAt
) {
    public SecurityReport {
        highestRiskEvents = highestRiskEvents == null ? List.of() : List.copyOf(highestRiskEvents);
        detectedAttackPatterns = detectedAttackPatterns == null ? List.of() : List.copyOf(detectedAttackPatterns);
        behavioralAnomalies = behavioralAnomalies == null ? List.of() : List.copyOf(behavioralAnomalies);
        recommendedPolicyChanges = recommendedPolicyChanges == null ? List.of() : List.copyOf(recommendedPolicyChanges);
    }
}
