package com.sentinel.domain;

import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.AgentState;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.ResourceSensitivity;
import com.sentinel.domain.enums.RiskBand;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The immutable record of one evaluated action — Sentinel's unit of telemetry. An event captures
 * the action, everything the pipeline computed about it (permissions, resource sensitivity, risk
 * factors, injection findings, anomaly signals, matched policy), the final {@link Decision}, and a
 * {@code previousEventId} link. Those links turn a run's events into a directed graph that the
 * escalation detector and timeline traverse.
 *
 * <p>Because the field count is large and the evaluator assembles an event incrementally, it is
 * built via {@link #builder()} rather than the canonical constructor.
 */
public record Event(
        String id,
        String runId,
        int sequence,
        Instant timestamp,
        String agentId,
        String agentName,
        ActionType actionType,
        String resource,
        String command,
        String intent,
        Map<String, String> parameters,
        Set<Capability> requiredCapabilities,
        Set<Capability> missingCapabilities,
        ResourceSensitivity resourceSensitivity,
        RiskAssessment riskAssessment,
        List<InjectionFinding> injectionFindings,
        AnomalyResult anomalyResult,
        PolicyDecision policyDecision,
        Decision decision,
        boolean executed,
        List<String> reasons,
        List<String> warnings,
        List<String> recommendations,
        String previousEventId,
        AgentState agentStateAfter,
        boolean inducedByInjection
) {
    public Event {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        requiredCapabilities = requiredCapabilities == null ? Set.of() : Set.copyOf(requiredCapabilities);
        missingCapabilities = missingCapabilities == null ? Set.of() : Set.copyOf(missingCapabilities);
        injectionFindings = injectionFindings == null ? List.of() : List.copyOf(injectionFindings);
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
        if (riskAssessment == null) {
            riskAssessment = RiskAssessment.none();
        }
        if (anomalyResult == null) {
            anomalyResult = AnomalyResult.none();
        }
    }

    public int riskScore() {
        return riskAssessment.score();
    }

    public RiskBand riskBand() {
        return riskAssessment.band();
    }

    public boolean hasInjection() {
        return !injectionFindings.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Mutable builder for the immutable {@link Event}. */
    public static final class Builder {
        private String id;
        private String runId;
        private int sequence;
        private Instant timestamp = Instant.now();
        private String agentId;
        private String agentName;
        private ActionType actionType;
        private String resource;
        private String command;
        private String intent;
        private Map<String, String> parameters = Map.of();
        private Set<Capability> requiredCapabilities = Set.of();
        private Set<Capability> missingCapabilities = Set.of();
        private ResourceSensitivity resourceSensitivity = ResourceSensitivity.PUBLIC;
        private RiskAssessment riskAssessment = RiskAssessment.none();
        private List<InjectionFinding> injectionFindings = List.of();
        private AnomalyResult anomalyResult = AnomalyResult.none();
        private PolicyDecision policyDecision = PolicyDecision.noMatch();
        private Decision decision = Decision.ALLOWED;
        private boolean executed;
        private List<String> reasons = List.of();
        private List<String> warnings = List.of();
        private List<String> recommendations = List.of();
        private String previousEventId;
        private AgentState agentStateAfter = AgentState.ACTIVE;
        private boolean inducedByInjection;

        public Builder id(String id) { this.id = id; return this; }
        public Builder runId(String runId) { this.runId = runId; return this; }
        public Builder sequence(int sequence) { this.sequence = sequence; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder agentId(String agentId) { this.agentId = agentId; return this; }
        public Builder agentName(String agentName) { this.agentName = agentName; return this; }
        public Builder actionType(ActionType actionType) { this.actionType = actionType; return this; }
        public Builder resource(String resource) { this.resource = resource; return this; }
        public Builder command(String command) { this.command = command; return this; }
        public Builder intent(String intent) { this.intent = intent; return this; }
        public Builder parameters(Map<String, String> parameters) { this.parameters = parameters; return this; }
        public Builder requiredCapabilities(Set<Capability> c) { this.requiredCapabilities = c; return this; }
        public Builder missingCapabilities(Set<Capability> c) { this.missingCapabilities = c; return this; }
        public Builder resourceSensitivity(ResourceSensitivity s) { this.resourceSensitivity = s; return this; }
        public Builder riskAssessment(RiskAssessment r) { this.riskAssessment = r; return this; }
        public Builder injectionFindings(List<InjectionFinding> f) { this.injectionFindings = f; return this; }
        public Builder anomalyResult(AnomalyResult a) { this.anomalyResult = a; return this; }
        public Builder policyDecision(PolicyDecision p) { this.policyDecision = p; return this; }
        public Builder decision(Decision d) { this.decision = d; return this; }
        public Builder executed(boolean e) { this.executed = e; return this; }
        public Builder reasons(List<String> r) { this.reasons = r; return this; }
        public Builder warnings(List<String> w) { this.warnings = w; return this; }
        public Builder recommendations(List<String> r) { this.recommendations = r; return this; }
        public Builder previousEventId(String id) { this.previousEventId = id; return this; }
        public Builder agentStateAfter(AgentState s) { this.agentStateAfter = s; return this; }
        public Builder inducedByInjection(boolean b) { this.inducedByInjection = b; return this; }

        public Event build() {
            return new Event(id, runId, sequence, timestamp, agentId, agentName, actionType, resource,
                    command, intent, parameters, requiredCapabilities, missingCapabilities,
                    resourceSensitivity, riskAssessment, injectionFindings, anomalyResult, policyDecision,
                    decision, executed, reasons, warnings, recommendations, previousEventId,
                    agentStateAfter, inducedByInjection);
        }
    }
}
