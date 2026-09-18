package com.sentinel.evaluation;

import com.sentinel.domain.PermissionCheck;
import com.sentinel.domain.PolicyDecision;
import com.sentinel.domain.RiskAssessment;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.PolicyEffect;
import com.sentinel.domain.enums.RiskBand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Combines the permission check, policy decision, and risk assessment into one final
 * {@link Decision}. The precedence is deliberate and defense-in-depth:
 *
 * <ol>
 *   <li>A missing capability is an <b>absolute block</b> — least privilege is not negotiable.</li>
 *   <li>A <b>never-allowed destructive command</b> (danger ≥ 0.9: {@code rm -rf /}, fork bombs,
 *       piping a download into a shell, reverse shells) is blocked regardless of everything else.</li>
 *   <li>An explicit policy <b>DENY</b> blocks.</li>
 *   <li><b>CRITICAL</b> risk blocks — the risk gate can veto even a permissive policy.</li>
 *   <li>A policy <b>REQUIRE_APPROVAL</b>, or <b>HIGH</b> risk, holds the action for a human.</li>
 *   <li>Otherwise the action is allowed, with warnings for WARN policies or MEDIUM risk.</li>
 * </ol>
 *
 * <p>Keeping this ordering in one small, pure method makes the whole allow/hold/block behavior
 * unit-testable in isolation and easy to explain in a review.
 */
@Component
public class DecisionResolver {

    /** Commands at or above this danger are never allowed, independent of the aggregate score. */
    private static final double NEVER_ALLOW_COMMAND_DANGER = 0.9;

    public Resolution resolve(PermissionCheck permissionCheck, PolicyDecision policyDecision,
                              RiskAssessment assessment, double commandDanger) {
        List<String> warnings = new ArrayList<>();

        if (!permissionCheck.satisfied()) {
            return new Resolution(Decision.BLOCKED, false, warnings);
        }
        if (commandDanger >= NEVER_ALLOW_COMMAND_DANGER) {
            return new Resolution(Decision.BLOCKED, false, warnings);
        }
        if (policyDecision.matched() && policyDecision.effect() == PolicyEffect.DENY) {
            return new Resolution(Decision.BLOCKED, false, warnings);
        }
        if (assessment.band() == RiskBand.CRITICAL) {
            return new Resolution(Decision.BLOCKED, false, warnings);
        }
        if (policyDecision.matched() && policyDecision.effect() == PolicyEffect.REQUIRE_APPROVAL) {
            return new Resolution(Decision.REQUIRES_APPROVAL, false, warnings);
        }
        if (assessment.band() == RiskBand.HIGH) {
            return new Resolution(Decision.REQUIRES_APPROVAL, false, warnings);
        }

        if (policyDecision.matched() && policyDecision.effect() == PolicyEffect.WARN) {
            warnings.add("Policy warning — " + policyDecision.reason());
        }
        if (assessment.band() == RiskBand.MEDIUM) {
            warnings.add("Medium-risk action allowed with monitoring.");
        }
        return new Resolution(Decision.ALLOWED, true, warnings);
    }
}
