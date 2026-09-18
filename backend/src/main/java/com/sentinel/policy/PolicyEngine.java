package com.sentinel.policy;

import com.sentinel.domain.Agent;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.Policy;
import com.sentinel.domain.PolicyDecision;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.ResourceSensitivity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Evaluates an action against the active policy set. A rule matches when <em>all</em> of its
 * populated conditions hold (empty conditions are wildcards); among matching rules, the one with
 * the strongest {@link com.sentinel.domain.enums.PolicyEffect} wins, ties broken by priority. This
 * "strongest effect wins" precedence means an explicit DENY always overrides a broad ALLOW.
 *
 * <p>The engine is pure logic over the policies it is handed — it holds no state and does no
 * persistence — which keeps policy authoring and storage entirely separate from enforcement.
 */
@Component
public class PolicyEngine {

    public PolicyDecision evaluate(Agent agent, AgentAction action, ResourceSensitivity sensitivity,
                                   Set<Capability> requiredCapabilities, List<Policy> policies) {
        return policies.stream()
                .filter(Policy::enabled)
                .filter(policy -> matches(policy, agent, action, sensitivity, requiredCapabilities))
                .max(Comparator
                        .comparingInt((Policy p) -> p.effect().ordinal())
                        .thenComparingInt(Policy::priority))
                .map(PolicyDecision::of)
                .orElseGet(PolicyDecision::noMatch);
    }

    private boolean matches(Policy policy, Agent agent, AgentAction action,
                            ResourceSensitivity sensitivity, Set<Capability> requiredCapabilities) {
        if (!policy.archetypes().isEmpty() && !policy.archetypes().contains(agent.archetype())) {
            return false;
        }
        if (!policy.actionTypes().isEmpty() && !policy.actionTypes().contains(action.type())) {
            return false;
        }
        if (policy.resourcePattern() != null && !policy.resourcePattern().isBlank()
                && !ResourceMatcher.matches(policy.resourcePattern(), action.resource())) {
            return false;
        }
        if (policy.minSensitivity() != null
                && sensitivity.getWeight() < policy.minSensitivity().getWeight()) {
            return false;
        }
        if (policy.requiredCapability() != null
                && !requiredCapabilities.contains(policy.requiredCapability())) {
            return false;
        }
        return true;
    }
}
