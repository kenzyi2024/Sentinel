package com.sentinel.domain.enums;

/**
 * The dimension of risk a {@link com.sentinel.domain.RiskFactor} belongs to. Each risk scorer
 * contributes factors under exactly one category, which lets the UI group an explanation by the
 * kind of concern it represents.
 */
public enum RiskCategory {
    PERMISSION("Permission & least privilege"),
    RESOURCE_SENSITIVITY("Resource sensitivity"),
    COMMAND_DANGER("Command danger"),
    PROMPT_INJECTION("Prompt injection"),
    BEHAVIORAL_ANOMALY("Behavioral anomaly"),
    POLICY_VIOLATION("Policy violation"),
    PRIVILEGE_ESCALATION("Privilege escalation");

    private final String label;

    RiskCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
