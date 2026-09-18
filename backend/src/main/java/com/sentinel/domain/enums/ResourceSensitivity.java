package com.sentinel.domain.enums;

/**
 * How sensitive a targeted resource is. Assigned by the {@code ResourceClassifier} from the
 * resource path/identifier, and used as a multiplier by the resource-sensitivity risk scorer.
 */
public enum ResourceSensitivity {
    PUBLIC("Public or non-sensitive resource", 1),
    INTERNAL("Internal project source or documentation", 2),
    SENSITIVE("Sensitive configuration or infrastructure", 3),
    SECRET("Credentials, keys, or secret material", 4);

    private final String description;
    private final int weight;

    ResourceSensitivity(String description, int weight) {
        this.description = description;
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    /** Sensitivity weight on a 1 (public) to 4 (secret) scale. */
    public int getWeight() {
        return weight;
    }
}
