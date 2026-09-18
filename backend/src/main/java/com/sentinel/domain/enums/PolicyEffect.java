package com.sentinel.domain.enums;

/**
 * The effect a matched {@link com.sentinel.domain.Policy} rule has on an action. Effects are
 * ordered by strength; when several rules match, the strongest effect wins (see
 * {@link #isStrongerThan}).
 */
public enum PolicyEffect {
    LOG_ONLY(0, "Record the action but take no restrictive action"),
    WARN(1, "Allow the action but surface a warning"),
    ALLOW(2, "Explicitly permit the action"),
    REQUIRE_APPROVAL(3, "Hold the action for human approval"),
    DENY(4, "Block the action outright");

    private final int strength;
    private final String description;

    PolicyEffect(int strength, String description) {
        this.strength = strength;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isStrongerThan(PolicyEffect other) {
        return this.strength > other.strength;
    }
}
