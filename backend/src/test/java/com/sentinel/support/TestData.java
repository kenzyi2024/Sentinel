package com.sentinel.support;

import com.sentinel.domain.Agent;
import com.sentinel.domain.Event;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.ResourceSensitivity;

import java.time.Instant;
import java.util.Set;

/**
 * Small builders shared across unit tests for constructing events and agents concisely.
 */
public final class TestData {

    private TestData() {
    }

    public static Event event(int sequence, ActionType type, ResourceSensitivity sensitivity, String resource) {
        return Event.builder()
                .id("e" + sequence)
                .runId("run")
                .sequence(sequence)
                .timestamp(Instant.EPOCH.plusSeconds(sequence))
                .agentId("a")
                .agentName("Agent")
                .actionType(type)
                .resource(resource)
                .resourceSensitivity(sensitivity)
                .previousEventId(sequence > 1 ? "e" + (sequence - 1) : null)
                .build();
    }

    public static Event event(int sequence, ActionType type, ResourceSensitivity sensitivity) {
        return event(sequence, type, sensitivity, "resource-" + sequence);
    }

    public static Event event(int sequence, ActionType type) {
        return event(sequence, type, ResourceSensitivity.INTERNAL);
    }

    public static Agent agent(AgentArchetype archetype, Capability... capabilities) {
        return new Agent("agent-test", "Test Agent", archetype, "goal", Set.of(capabilities));
    }
}
