package com.sentinel.domain;

import com.sentinel.domain.enums.PolicyEffect;

/**
 * The outcome of evaluating an action against the policy set: which rule (if any) matched and the
 * effect it prescribes. When no rule matches, {@link #noMatch()} yields a permissive default that
 * lets the risk engine drive the final decision.
 *
 * @param matched    whether a policy rule matched
 * @param policyId   id of the matched rule, or {@code null}
 * @param policyName name of the matched rule, or {@code null}
 * @param effect     the effect to apply
 * @param reason     the rule's justification
 */
public record PolicyDecision(
        boolean matched,
        String policyId,
        String policyName,
        PolicyEffect effect,
        String reason
) {
    public static PolicyDecision noMatch() {
        return new PolicyDecision(false, null, null, PolicyEffect.ALLOW,
                "No explicit policy matched; default posture applied.");
    }

    public static PolicyDecision of(Policy policy) {
        return new PolicyDecision(true, policy.id(), policy.name(), policy.effect(), policy.reason());
    }
}
