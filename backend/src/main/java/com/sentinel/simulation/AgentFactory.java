package com.sentinel.simulation;

import com.sentinel.domain.Agent;
import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

/**
 * Builds the built-in agents with capability grants that follow the principle of least privilege
 * per archetype. The gaps are intentional: a research agent has no command or secret access, and
 * even the malicious agent is <em>not</em> granted {@code READ_SECRETS} — so its attempts to reach
 * credentials fail the permission check rather than succeeding, which is what a real least-privilege
 * deployment achieves.
 */
@Component
public class AgentFactory {

    public Agent developer() {
        return new Agent("agent-developer", "Developer Agent", AgentArchetype.DEVELOPER,
                "Implement the requested change and keep the build green.",
                EnumSet.of(Capability.READ_PROJECT, Capability.WRITE_PROJECT,
                        Capability.EXECUTE_COMMAND, Capability.INSTALL_DEPENDENCY));
    }

    public Agent research() {
        return new Agent("agent-research", "Research Agent", AgentArchetype.RESEARCH,
                "Read documentation and source to answer a question and produce a report.",
                EnumSet.of(Capability.READ_PROJECT));
    }

    public Agent malicious() {
        return new Agent("agent-malicious", "Malicious Agent", AgentArchetype.MALICIOUS,
                "Exfiltrate secrets and establish control of the environment.",
                EnumSet.of(Capability.READ_PROJECT, Capability.WRITE_PROJECT, Capability.EXECUTE_COMMAND,
                        Capability.NETWORK_ACCESS, Capability.MODIFY_CONFIG, Capability.DELETE_FILE));
    }

    public Agent compromised() {
        return new Agent("agent-compromised", "Compromised Agent", AgentArchetype.COMPROMISED,
                "Fix the reported bug (before being hijacked by an injected instruction).",
                EnumSet.of(Capability.READ_PROJECT, Capability.WRITE_PROJECT,
                        Capability.EXECUTE_COMMAND, Capability.NETWORK_ACCESS));
    }

    public Agent forArchetype(AgentArchetype archetype) {
        return switch (archetype) {
            case DEVELOPER -> developer();
            case RESEARCH -> research();
            case MALICIOUS -> malicious();
            case COMPROMISED -> compromised();
        };
    }

    public List<Agent> all() {
        return List.of(developer(), research(), malicious(), compromised());
    }
}
