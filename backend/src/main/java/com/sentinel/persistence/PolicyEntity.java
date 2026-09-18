package com.sentinel.persistence;

import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.PolicyEffect;
import com.sentinel.domain.enums.ResourceSensitivity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA persistence view of a {@link com.sentinel.domain.Policy}. Set-valued conditions (archetypes,
 * action types) are stored as comma-separated enum names — adequate for a small, human-authored
 * policy set and simpler than join tables.
 */
@Entity
@Table(name = "policies")
public class PolicyEntity {

    @Id
    private String id;

    private String name;
    private int priority;

    /** Comma-separated {@link com.sentinel.domain.enums.AgentArchetype} names; blank = any. */
    private String archetypes;

    /** Comma-separated {@link com.sentinel.domain.enums.ActionType} names; blank = any. */
    private String actionTypes;

    private String resourcePattern;

    @Enumerated(EnumType.STRING)
    private ResourceSensitivity minSensitivity;

    @Enumerated(EnumType.STRING)
    private Capability requiredCapability;

    @Enumerated(EnumType.STRING)
    private PolicyEffect effect;

    @Column(length = 1000)
    private String reason;

    private boolean enabled;

    protected PolicyEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getArchetypes() {
        return archetypes;
    }

    public void setArchetypes(String archetypes) {
        this.archetypes = archetypes;
    }

    public String getActionTypes() {
        return actionTypes;
    }

    public void setActionTypes(String actionTypes) {
        this.actionTypes = actionTypes;
    }

    public String getResourcePattern() {
        return resourcePattern;
    }

    public void setResourcePattern(String resourcePattern) {
        this.resourcePattern = resourcePattern;
    }

    public ResourceSensitivity getMinSensitivity() {
        return minSensitivity;
    }

    public void setMinSensitivity(ResourceSensitivity minSensitivity) {
        this.minSensitivity = minSensitivity;
    }

    public Capability getRequiredCapability() {
        return requiredCapability;
    }

    public void setRequiredCapability(Capability requiredCapability) {
        this.requiredCapability = requiredCapability;
    }

    public PolicyEffect getEffect() {
        return effect;
    }

    public void setEffect(PolicyEffect effect) {
        this.effect = effect;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
