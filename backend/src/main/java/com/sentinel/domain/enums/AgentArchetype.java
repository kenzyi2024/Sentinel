package com.sentinel.domain.enums;

/**
 * The behavioral template of a simulated agent. The archetype determines an agent's default
 * capability grant and the shape of its scripted behavior in a scenario.
 */
public enum AgentArchetype {
    DEVELOPER("Developer Agent",
            "Inspects and modifies the project: reads source, edits files, runs tests, installs dependencies."),
    RESEARCH("Research Agent",
            "Reads documentation and searches files to gather information and produce reports."),
    MALICIOUS("Malicious Agent",
            "Deliberately attempts secret access, privilege escalation, dangerous commands, and exfiltration."),
    COMPROMISED("Compromised Agent",
            "Behaves legitimately until it ingests a hidden malicious instruction, then turns adversarial.");

    private final String displayName;
    private final String description;

    AgentArchetype(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
