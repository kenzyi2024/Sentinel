package com.sentinel.simulation;

import com.sentinel.domain.Agent;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.ScenarioInfo;

import java.util.List;

/**
 * A runnable scenario: its shareable {@link ScenarioInfo}, the {@link Agent} that acts, and the
 * deterministic, ordered list of actions the agent attempts. Because the script is fixed, a run is
 * fully reproducible — the same scenario always produces the same decisions, which is what makes
 * the demos reliable and the behavior testable.
 */
public record ScenarioDefinition(ScenarioInfo info, Agent agent, List<AgentAction> actions) {
    public ScenarioDefinition {
        actions = List.copyOf(actions);
    }
}
