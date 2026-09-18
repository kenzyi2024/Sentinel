package com.sentinel.domain;

import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;

import java.util.Set;

/**
 * A simulated AI agent: an autonomous actor with a declared goal and an explicit, minimal set of
 * capabilities. An agent never accesses resources directly — every action it attempts is routed
 * through Sentinel's evaluation pipeline.
 *
 * @param id           stable identifier
 * @param name         display name
 * @param archetype    behavioral template
 * @param declaredGoal what the agent claims to be trying to accomplish
 * @param capabilities the capabilities granted to this agent (its authority)
 */
public record Agent(
        String id,
        String name,
        AgentArchetype archetype,
        String declaredGoal,
        Set<Capability> capabilities
) {
    public Agent {
        capabilities = capabilities == null ? Set.of() : Set.copyOf(capabilities);
    }

    public boolean hasCapability(Capability capability) {
        return capabilities.contains(capability);
    }
}
