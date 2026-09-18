package com.sentinel.domain.enums;

/**
 * Generic severity scale used by prompt-injection findings and report highlights. The numeric
 * weight lets several findings be aggregated into a single normalized signal.
 */
public enum Severity {
    INFO(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int weight;

    Severity(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    /** Highest severity ceiling, used to normalize aggregate severity to [0, 1]. */
    public static int maxWeight() {
        return CRITICAL.weight;
    }
}
