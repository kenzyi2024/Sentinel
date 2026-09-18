package com.sentinel.domain;

import com.sentinel.domain.enums.Capability;

import java.util.Set;

/**
 * The result of checking an action against an agent's granted capabilities.
 *
 * @param required the capabilities the action requires
 * @param granted  the capabilities the agent holds
 * @param missing  required capabilities the agent does not hold (empty when satisfied)
 */
public record PermissionCheck(
        Set<Capability> required,
        Set<Capability> granted,
        Set<Capability> missing
) {
    public PermissionCheck {
        required = required == null ? Set.of() : Set.copyOf(required);
        granted = granted == null ? Set.of() : Set.copyOf(granted);
        missing = missing == null ? Set.of() : Set.copyOf(missing);
    }

    /** True when the agent holds every required capability. */
    public boolean satisfied() {
        return missing.isEmpty();
    }
}
