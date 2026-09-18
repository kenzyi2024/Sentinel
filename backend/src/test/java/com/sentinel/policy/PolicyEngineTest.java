package com.sentinel.policy;

import com.sentinel.domain.Agent;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.Policy;
import com.sentinel.domain.PolicyDecision;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.PolicyEffect;
import com.sentinel.domain.enums.ResourceSensitivity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.sentinel.support.TestData.agent;
import static org.assertj.core.api.Assertions.assertThat;

class PolicyEngineTest {

    private final PolicyEngine engine = new PolicyEngine();

    private final Policy researchNoSecrets = Policy.builder("p-research-secrets")
            .archetypes(AgentArchetype.RESEARCH)
            .minSensitivity(ResourceSensitivity.SECRET)
            .effect(PolicyEffect.DENY)
            .reason("Research agents cannot read secrets.")
            .build();

    @Test
    void deniesResearchAgentReadingSecrets() {
        Agent research = agent(AgentArchetype.RESEARCH, Capability.READ_PROJECT);
        PolicyDecision decision = engine.evaluate(research, AgentAction.read(".env", "c", "read"),
                ResourceSensitivity.SECRET, Set.of(Capability.READ_PROJECT, Capability.READ_SECRETS),
                List.of(researchNoSecrets));

        assertThat(decision.matched()).isTrue();
        assertThat(decision.effect()).isEqualTo(PolicyEffect.DENY);
    }

    @Test
    void noMatchLeavesADefaultAllow() {
        Agent developer = agent(AgentArchetype.DEVELOPER, Capability.READ_PROJECT);
        PolicyDecision decision = engine.evaluate(developer, AgentAction.read("src/App.java", "c", "read"),
                ResourceSensitivity.INTERNAL, Set.of(Capability.READ_PROJECT), List.of(researchNoSecrets));

        assertThat(decision.matched()).isFalse();
        assertThat(decision.effect()).isEqualTo(PolicyEffect.ALLOW);
    }

    @Test
    void strongestEffectWinsAmongMatches() {
        Policy warn = Policy.builder("warn").actionTypes(ActionType.NETWORK_REQUEST)
                .effect(PolicyEffect.WARN).reason("warn").build();
        Policy deny = Policy.builder("deny").actionTypes(ActionType.NETWORK_REQUEST)
                .effect(PolicyEffect.DENY).reason("deny").build();

        Agent malicious = agent(AgentArchetype.MALICIOUS, Capability.NETWORK_ACCESS);
        PolicyDecision decision = engine.evaluate(malicious, AgentAction.network("evil.example", "send"),
                ResourceSensitivity.PUBLIC, Set.of(Capability.NETWORK_ACCESS), List.of(warn, deny));

        assertThat(decision.effect()).isEqualTo(PolicyEffect.DENY);
    }

    @Test
    void disabledPoliciesAreIgnored() {
        Policy disabled = Policy.builder("p-research-secrets")
                .archetypes(AgentArchetype.RESEARCH).minSensitivity(ResourceSensitivity.SECRET)
                .effect(PolicyEffect.DENY).reason("x").enabled(false).build();
        Agent research = agent(AgentArchetype.RESEARCH, Capability.READ_PROJECT);

        PolicyDecision decision = engine.evaluate(research, AgentAction.read(".env", "c", "read"),
                ResourceSensitivity.SECRET, Set.of(Capability.READ_PROJECT), List.of(disabled));

        assertThat(decision.matched()).isFalse();
    }
}
