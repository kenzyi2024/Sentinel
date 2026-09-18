package com.sentinel.domain.enums;

/**
 * Categories of prompt-injection technique the detector recognizes. Each detection rule is
 * tagged with a category so findings can be grouped and explained.
 */
public enum InjectionCategory {
    INSTRUCTION_OVERRIDE("Attempts to override, ignore, or supersede prior instructions"),
    ROLE_MANIPULATION("Attempts to reassign the agent's role, persona, or privileges"),
    CONTEXT_SWITCH("Fake delimiters or system markers that try to switch context"),
    DATA_EXFILTRATION("Attempts to send data to an external destination"),
    SECRET_ACCESS("Attempts to read secrets, credentials, or keys"),
    SECURITY_CONTROL_DISABLE("Attempts to disable logging, monitoring, or safety controls"),
    IMPERATIVE_INJECTION("Imperative commands embedded in what should be inert data"),
    OBFUSCATION("Encoded, spaced, or otherwise obfuscated content hiding instructions");

    private final String description;

    InjectionCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
