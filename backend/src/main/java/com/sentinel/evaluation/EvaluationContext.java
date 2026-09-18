package com.sentinel.evaluation;

import com.sentinel.detection.CommandAnalysis;
import com.sentinel.detection.EscalationSignal;
import com.sentinel.domain.Agent;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.AnomalyResult;
import com.sentinel.domain.Event;
import com.sentinel.domain.InjectionFinding;
import com.sentinel.domain.PermissionCheck;
import com.sentinel.domain.PolicyDecision;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.ResourceSensitivity;

import java.util.List;
import java.util.Set;

/**
 * The fully-gathered set of signals for a single action, assembled by the {@code ActionEvaluator}
 * before risk scoring. Every risk scorer and the decision resolver read from this context, so the
 * inputs to a decision are captured in one immutable place — which is exactly what later lets the
 * UI reconstruct <em>why</em> a decision was made.
 *
 * <p>Note the ordering: permissions, resource classification, injection findings, command
 * analysis, anomaly, escalation, and the matched policy are all computed <em>before</em> risk, so
 * a scorer may reference any of them (e.g. the policy-violation scorer reads {@code policyDecision}).
 * The numeric risk assessment and the final decision are produced afterward.
 */
public record EvaluationContext(
        Agent agent,
        AgentAction action,
        ResourceSensitivity sensitivity,
        Set<Capability> requiredCapabilities,
        PermissionCheck permissionCheck,
        List<InjectionFinding> injectionFindings,
        CommandAnalysis commandAnalysis,
        AnomalyResult anomalyResult,
        EscalationSignal escalationSignal,
        PolicyDecision policyDecision,
        List<Event> priorEvents
) {
    public EvaluationContext {
        injectionFindings = injectionFindings == null ? List.of() : List.copyOf(injectionFindings);
        priorEvents = priorEvents == null ? List.of() : List.copyOf(priorEvents);
    }

    public boolean hasInjection() {
        return !injectionFindings.isEmpty();
    }
}
