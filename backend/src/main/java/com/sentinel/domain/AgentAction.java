package com.sentinel.domain;

import com.sentinel.domain.enums.ActionType;

import java.util.Map;

/**
 * An action a simulated agent <em>attempts</em> — the intent, before Sentinel evaluates it.
 * Actions are immutable value objects; the static factory methods keep scenario scripts and
 * tests readable.
 *
 * @param type               the kind of action
 * @param resource           the target: a file path, host, or command subject
 * @param command            the full shell command when {@code type == EXECUTE_COMMAND}, else {@code null}
 * @param content            material to be scanned for injection (file content read or written), else {@code null}
 * @param parameters         additional structured parameters
 * @param intent             a human-readable description of what the agent is trying to do
 * @param inducedByInjection true when a scenario models this action as a consequence of ingested injection
 */
public record AgentAction(
        ActionType type,
        String resource,
        String command,
        String content,
        Map<String, String> parameters,
        String intent,
        boolean inducedByInjection
) {
    public AgentAction {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    /** Returns a copy of this action flagged as induced by a prior prompt injection. */
    public AgentAction induced() {
        return new AgentAction(type, resource, command, content, parameters, intent, true);
    }

    public static AgentAction list(String resource, String intent) {
        return new AgentAction(ActionType.LIST_DIRECTORY, resource, null, null, Map.of(), intent, false);
    }

    public static AgentAction read(String resource, String intent) {
        return new AgentAction(ActionType.READ_FILE, resource, null, null, Map.of(), intent, false);
    }

    /** A file read whose contents are supplied so the injection detector can scan them. */
    public static AgentAction read(String resource, String content, String intent) {
        return new AgentAction(ActionType.READ_FILE, resource, null, content, Map.of(), intent, false);
    }

    public static AgentAction search(String query, String intent) {
        return new AgentAction(ActionType.SEARCH_FILES, query, null, null, Map.of(), intent, false);
    }

    public static AgentAction readEnv(String resource, String intent) {
        return new AgentAction(ActionType.READ_ENV, resource, null, null, Map.of(), intent, false);
    }

    public static AgentAction write(String resource, String content, String intent) {
        return new AgentAction(ActionType.WRITE_FILE, resource, null, content, Map.of(), intent, false);
    }

    public static AgentAction delete(String resource, String intent) {
        return new AgentAction(ActionType.DELETE_FILE, resource, null, null, Map.of(), intent, false);
    }

    public static AgentAction modifyConfig(String resource, String content, String intent) {
        return new AgentAction(ActionType.MODIFY_CONFIG, resource, null, content, Map.of(), intent, false);
    }

    public static AgentAction installDependency(String name, String intent) {
        return new AgentAction(ActionType.INSTALL_DEPENDENCY, name, null, null, Map.of(), intent, false);
    }

    public static AgentAction exec(String command, String intent) {
        return new AgentAction(ActionType.EXECUTE_COMMAND, command, command, null, Map.of(), intent, false);
    }

    public static AgentAction network(String host, String intent) {
        return new AgentAction(ActionType.NETWORK_REQUEST, host, null, null, Map.of(), intent, false);
    }

    /** The text that should be scanned for injection: explicit content, else the command. */
    public String scannableText() {
        if (content != null && !content.isBlank()) {
            return content;
        }
        return command;
    }
}
