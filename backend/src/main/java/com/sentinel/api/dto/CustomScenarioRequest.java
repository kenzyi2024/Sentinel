package com.sentinel.api.dto;

import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Request body for running a user-defined scenario: a custom agent (archetype + capability grant)
 * and an ordered list of actions to push through the real evaluation engine.
 */
public record CustomScenarioRequest(
        @NotBlank(message = "name is required") String name,
        @NotNull(message = "archetype is required") AgentArchetype archetype,
        Set<Capability> capabilities,
        @NotEmpty(message = "at least one action is required") List<@Valid CustomActionRequest> actions
) {
    public Set<Capability> capabilities() {
        return capabilities == null ? Set.of() : capabilities;
    }
}
