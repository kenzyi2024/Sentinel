package com.sentinel.domain.enums;

/**
 * Capability-based permissions granted to an agent. This models the principle of least
 * privilege: an agent holds a minimal, explicit set of capabilities, and an action is only
 * permitted if the agent holds every capability the action requires.
 *
 * <p>The {@code privilegeWeight} expresses how dangerous it is to hold this capability, and is
 * used by the risk engine when an agent exercises (or attempts to exceed) its granted powers.
 */
public enum Capability {
    READ_PROJECT("Read non-sensitive project files", 1),
    WRITE_PROJECT("Create or modify project files", 3),
    DELETE_FILE("Delete files", 5),
    INSTALL_DEPENDENCY("Install third-party dependencies", 5),
    MODIFY_CONFIG("Modify configuration", 6),
    EXECUTE_COMMAND("Execute shell commands", 7),
    NETWORK_ACCESS("Make outbound network requests", 6),
    READ_SECRETS("Read credentials and secret material", 9);

    private final String description;
    private final int privilegeWeight;

    Capability(String description, int privilegeWeight) {
        this.description = description;
        this.privilegeWeight = privilegeWeight;
    }

    public String getDescription() {
        return description;
    }

    /** Relative danger of holding this capability, on a 1 (benign) to 10 (highly sensitive) scale. */
    public int getPrivilegeWeight() {
        return privilegeWeight;
    }
}
