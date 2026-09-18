package com.sentinel.domain;

import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.PolicyEffect;
import com.sentinel.domain.enums.ResourceSensitivity;

import java.util.Set;

/**
 * A declarative security policy rule. A rule matches an action when <em>all</em> of its populated
 * conditions hold; empty/{@code null} conditions are treated as wildcards. Matching itself lives in
 * {@code PolicyEngine} so the domain stays free of evaluation logic — this record is pure data,
 * which is what lets policies be stored, edited, and reloaded without touching the core engine.
 *
 * @param id                stable identifier
 * @param name              human-readable name
 * @param priority          tie-breaker among rules of equal effect strength (higher wins)
 * @param archetypes        agent archetypes the rule applies to (empty = any)
 * @param actionTypes       action types the rule applies to (empty = any)
 * @param resourcePattern   glob-style resource pattern (null/blank = any)
 * @param minSensitivity    match only resources at least this sensitive (null = any)
 * @param requiredCapability match only actions that require this capability (null = any)
 * @param effect            the effect to apply when the rule matches
 * @param reason            the justification surfaced in explanations
 * @param enabled           whether the rule participates in evaluation
 */
public record Policy(
        String id,
        String name,
        int priority,
        Set<AgentArchetype> archetypes,
        Set<ActionType> actionTypes,
        String resourcePattern,
        ResourceSensitivity minSensitivity,
        Capability requiredCapability,
        PolicyEffect effect,
        String reason,
        boolean enabled
) {
    public Policy {
        archetypes = archetypes == null ? Set.of() : Set.copyOf(archetypes);
        actionTypes = actionTypes == null ? Set.of() : Set.copyOf(actionTypes);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    /** Fluent builder — keeps the policy seed configuration readable. */
    public static final class Builder {
        private final String id;
        private String name;
        private int priority = 0;
        private Set<AgentArchetype> archetypes = Set.of();
        private Set<ActionType> actionTypes = Set.of();
        private String resourcePattern;
        private ResourceSensitivity minSensitivity;
        private Capability requiredCapability;
        private PolicyEffect effect = PolicyEffect.ALLOW;
        private String reason = "";
        private boolean enabled = true;

        private Builder(String id) {
            this.id = id;
            this.name = id;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder archetypes(AgentArchetype... archetypes) {
            this.archetypes = Set.of(archetypes);
            return this;
        }

        public Builder actionTypes(ActionType... actionTypes) {
            this.actionTypes = Set.of(actionTypes);
            return this;
        }

        public Builder resourcePattern(String resourcePattern) {
            this.resourcePattern = resourcePattern;
            return this;
        }

        public Builder minSensitivity(ResourceSensitivity minSensitivity) {
            this.minSensitivity = minSensitivity;
            return this;
        }

        public Builder requiredCapability(Capability requiredCapability) {
            this.requiredCapability = requiredCapability;
            return this;
        }

        public Builder effect(PolicyEffect effect) {
            this.effect = effect;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Policy build() {
            return new Policy(id, name, priority, archetypes, actionTypes, resourcePattern,
                    minSensitivity, requiredCapability, effect, reason, enabled);
        }
    }
}
