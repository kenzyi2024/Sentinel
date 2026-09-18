package com.sentinel.api.dto;

import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.PolicyEffect;
import com.sentinel.domain.enums.ResourceSensitivity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * Request body for creating or updating a policy. Enum-valued fields are deserialized from their
 * names; empty collections and {@code null} scalar conditions are treated as wildcards.
 */
public record PolicyRequest(
        @NotBlank(message = "name is required") String name,
        int priority,
        Set<AgentArchetype> archetypes,
        Set<ActionType> actionTypes,
        String resourcePattern,
        ResourceSensitivity minSensitivity,
        Capability requiredCapability,
        @NotNull(message = "effect is required") PolicyEffect effect,
        @NotBlank(message = "reason is required") String reason,
        Boolean enabled
) {
    public Set<AgentArchetype> archetypes() {
        return archetypes == null ? Set.of() : archetypes;
    }

    public Set<ActionType> actionTypes() {
        return actionTypes == null ? Set.of() : actionTypes;
    }

    public boolean enabledOrDefault() {
        return enabled == null || enabled;
    }
}
