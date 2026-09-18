package com.sentinel.domain.enums;

/**
 * The kinds of action a simulated agent can attempt. Each action an agent emits carries
 * exactly one {@code ActionType}; the security pipeline uses it to look up required
 * {@link Capability capabilities} and to route to the appropriate risk scorers.
 */
public enum ActionType {
    LIST_DIRECTORY("Enumerate the contents of a directory", false),
    READ_FILE("Read the contents of a file", false),
    SEARCH_FILES("Search across project files", false),
    READ_ENV("Read environment variables / secrets", false),
    WRITE_FILE("Create or overwrite a file", true),
    DELETE_FILE("Delete a file", true),
    MODIFY_CONFIG("Change project or tooling configuration", true),
    INSTALL_DEPENDENCY("Install a third-party dependency", true),
    EXECUTE_COMMAND("Run a shell command", true),
    SPAWN_PROCESS("Start a long-lived background process", true),
    NETWORK_REQUEST("Make an outbound network request", true);

    private final String description;
    private final boolean mutating;

    ActionType(String description, boolean mutating) {
        this.description = description;
        this.mutating = mutating;
    }

    public String getDescription() {
        return description;
    }

    /** Whether the action changes state (writes, deletes, executes) rather than only observing. */
    public boolean isMutating() {
        return mutating;
    }
}
