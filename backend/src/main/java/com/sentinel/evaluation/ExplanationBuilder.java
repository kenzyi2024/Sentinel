package com.sentinel.evaluation;

import com.sentinel.domain.RiskAssessment;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.Decision;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Assembles the "why this happened" explanation for an evaluated action from structured data only.
 * The reason bullets are the decision headline followed by the highest-contributing risk factors
 * (which already carry human-readable justifications); the recommendations are derived from the
 * concrete signals that fired. No language model is involved — the same inputs always yield the
 * same explanation, which is the point.
 */
@Component
public class ExplanationBuilder {

    private static final int MAX_REASON_FACTORS = 4;

    public Explanation explain(EvaluationContext context, RiskAssessment assessment, Resolution resolution) {
        List<String> reasons = new ArrayList<>();
        reasons.add(headline(resolution.decision(), assessment));
        for (RiskFactor factor : assessment.topFactors()) {
            if (reasons.size() > MAX_REASON_FACTORS) {
                break;
            }
            reasons.add(factor.explanation());
        }
        if (reasons.size() == 1) {
            reasons.add("No elevated risk signals — the action is consistent with the agent's role and the active policy.");
        }

        List<String> recommendations = new ArrayList<>();
        if (!context.permissionCheck().satisfied()) {
            String missing = context.permissionCheck().missing().stream()
                    .map(Enum::name).collect(Collectors.joining(", "));
            recommendations.add("Uphold least privilege: do not grant [" + missing + "] unless this agent genuinely requires it.");
        }
        if (context.hasInjection()) {
            recommendations.add("Treat ingested file and tool output as untrusted data — never follow instructions "
                    + "embedded in it (this is indirect prompt injection).");
        }
        if (context.escalationSignal() != null && context.escalationSignal().signal() > 0.0) {
            recommendations.add("Gate outbound network and configuration changes behind human approval, and alert on "
                    + "reconnaissance → discovery → exfiltration sequences.");
        }
        if (context.anomalyResult() != null && context.anomalyResult().enumerationDetected()) {
            recommendations.add("Rate-limit bulk file enumeration and review it before allowing follow-on access.");
        }
        if (context.commandAnalysis() != null && context.commandAnalysis().danger() > 0.0) {
            recommendations.add("Execute agent shell commands in a sandbox with an allowlist; deny destructive or "
                    + "remote-execution patterns outright.");
        }
        if (resolution.decision() == Decision.REQUIRES_APPROVAL) {
            recommendations.add("A human reviewer should approve or reject this action before it proceeds.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("No changes recommended — behavior is within policy.");
        }
        return new Explanation(reasons, recommendations);
    }

    private String headline(Decision decision, RiskAssessment assessment) {
        String verb = switch (decision) {
            case BLOCKED -> "BLOCKED";
            case REQUIRES_APPROVAL -> "HELD FOR APPROVAL";
            case ALLOWED -> "ALLOWED";
        };
        return verb + " · risk " + assessment.score() + "/100 (" + assessment.band() + ")";
    }
}
