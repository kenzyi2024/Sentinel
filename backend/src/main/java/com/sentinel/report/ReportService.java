package com.sentinel.report;

import com.sentinel.config.RiskModelProperties;
import com.sentinel.detection.EscalationDetector;
import com.sentinel.domain.Agent;
import com.sentinel.domain.AttackPattern;
import com.sentinel.domain.Event;
import com.sentinel.domain.EventSummary;
import com.sentinel.domain.ScenarioInfo;
import com.sentinel.domain.SecurityReport;
import com.sentinel.domain.enums.AgentState;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.PolicyEffect;
import com.sentinel.domain.enums.RunStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Produces the post-run {@link SecurityReport}. Every figure is computed deterministically from the
 * run's events — counts, risk aggregates, highest-risk highlights, detected attack patterns (via the
 * escalation detector's graph analysis), behavioral anomalies, and concrete policy recommendations.
 * No language model or external service is used.
 */
@Service
public class ReportService {

    private static final int HIGHEST_RISK_LIMIT = 5;

    private final EscalationDetector escalationDetector;
    private final RiskModelProperties riskProperties;

    public ReportService(EscalationDetector escalationDetector, RiskModelProperties riskProperties) {
        this.escalationDetector = escalationDetector;
        this.riskProperties = riskProperties;
    }

    public SecurityReport generate(String runId, ScenarioInfo scenario, Agent agent,
                                   RunStatus status, AgentState finalState, List<Event> events) {
        int allowed = 0;
        int blocked = 0;
        int requiresApproval = 0;
        int warningsRaised = 0;
        int policyViolations = 0;
        int injectionFindings = 0;
        int anomalies = 0;
        int maxRiskScore = 0;
        long riskSum = 0;

        for (Event event : events) {
            switch (event.decision()) {
                case ALLOWED -> allowed++;
                case BLOCKED -> blocked++;
                case REQUIRES_APPROVAL -> requiresApproval++;
            }
            warningsRaised += event.warnings().size();
            if (isRestrictive(event)) {
                policyViolations++;
            }
            injectionFindings += event.injectionFindings().size();
            if (event.anomalyResult().score() > 0.0) {
                anomalies++;
            }
            maxRiskScore = Math.max(maxRiskScore, event.riskScore());
            riskSum += event.riskScore();
        }

        double averageRiskScore = events.isEmpty() ? 0.0 : (double) riskSum / events.size();

        List<EventSummary> highestRisk = events.stream()
                .sorted(Comparator.comparingInt(Event::riskScore).reversed())
                .limit(HIGHEST_RISK_LIMIT)
                .map(EventSummary::from)
                .toList();

        List<AttackPattern> patterns = escalationDetector.detectPatterns(events);
        List<String> patternStrings = patterns.stream()
                .map(p -> p.name() + " — " + p.description() + " (events " + p.eventSequences() + ")")
                .toList();

        List<String> behavioralAnomalies = events.stream()
                .filter(e -> e.anomalyResult().score() > 0.0)
                .map(e -> "Action #" + e.sequence() + ": " + String.join(" ", e.anomalyResult().reasons()))
                .limit(10)
                .toList();

        List<String> recommendations = recommendations(agent, events, patterns, blocked, injectionFindings);

        return new SecurityReport(runId, scenario.id(), scenario.name(), agent.name(), agent.archetype(),
                status, finalState, events.size(), allowed, blocked, requiresApproval, warningsRaised,
                policyViolations, injectionFindings, anomalies, maxRiskScore, averageRiskScore,
                riskProperties.band(maxRiskScore), highestRisk, patternStrings, behavioralAnomalies,
                recommendations, Instant.now());
    }

    private boolean isRestrictive(Event event) {
        if (!event.policyDecision().matched()) {
            return false;
        }
        PolicyEffect effect = event.policyDecision().effect();
        return effect == PolicyEffect.DENY || effect == PolicyEffect.REQUIRE_APPROVAL || effect == PolicyEffect.WARN;
    }

    private List<String> recommendations(Agent agent, List<Event> events, List<AttackPattern> patterns,
                                         int blocked, int injectionFindings) {
        List<String> recommendations = new ArrayList<>();

        Set<Capability> unused = unusedCapabilities(agent, events);
        if (!unused.isEmpty()) {
            String names = unused.stream().map(Enum::name).collect(Collectors.joining(", "));
            recommendations.add("Least privilege: " + agent.name() + " holds capabilities it never exercised in this "
                    + "run [" + names + "]; consider revoking them.");
        }
        if (injectionFindings > 0) {
            recommendations.add("Indirect prompt injection was detected in ingested content. Continue treating file "
                    + "and tool output as untrusted data, and never execute instructions embedded in it.");
        }
        boolean exfilChain = patterns.stream().anyMatch(p -> p.id().equals("chain.recon-discovery-exfil"));
        if (exfilChain) {
            recommendations.add("Keep outbound network and configuration changes gated behind human approval, and "
                    + "alert on the reconnaissance → discovery → exfiltration sequence observed here.");
        }
        if (blocked > 0) {
            recommendations.add(blocked + " action(s) were blocked. Review the highest-risk events and confirm the "
                    + "responsible policies and capability grants are correct.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("No policy changes recommended — the run stayed within the configured policy.");
        }
        return recommendations;
    }

    private Set<Capability> unusedCapabilities(Agent agent, List<Event> events) {
        if (agent.capabilities().isEmpty()) {
            return Set.of();
        }
        Set<Capability> used = events.stream()
                .flatMap(e -> e.requiredCapabilities().stream())
                .collect(Collectors.toSet());
        EnumSet<Capability> unused = EnumSet.copyOf(agent.capabilities());
        unused.removeAll(used);
        return unused;
    }
}
