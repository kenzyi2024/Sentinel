package com.sentinel.domain;

import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;

import java.util.Set;

/**
 * Serializable metadata describing a scenario in the Scenario Lab: what it demonstrates, the threat
 * model, the acting agent's archetype and granted capabilities, and how many actions it runs. The
 * executable script lives alongside this in the simulation layer; this is the shareable summary.
 */
public record ScenarioInfo(
        String id,
        String name,
        String description,
        String objective,
        String threatModel,
        AgentArchetype archetype,
        String expectedBehavior,
        Set<Capability> grantedCapabilities,
        int actionCount
) {
    public ScenarioInfo {
        grantedCapabilities = grantedCapabilities == null ? Set.of() : Set.copyOf(grantedCapabilities);
    }
}
