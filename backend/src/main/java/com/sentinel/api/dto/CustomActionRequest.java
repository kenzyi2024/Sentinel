package com.sentinel.api.dto;

import com.sentinel.domain.AgentAction;
import com.sentinel.domain.enums.ActionType;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * One action in a user-defined custom scenario.
 */
public record CustomActionRequest(
        @NotNull(message = "action type is required") ActionType type,
        String resource,
        String command,
        String content,
        String intent,
        boolean inducedByInjection
) {
    public AgentAction toAction() {
        // For command actions, fall back to the command as the resource when none is given.
        String resolvedResource = (resource == null || resource.isBlank()) && command != null
                ? command
                : (resource == null ? "" : resource);
        String resolvedIntent = (intent == null || intent.isBlank()) ? type.getDescription() : intent;
        return new AgentAction(type, resolvedResource, command, content, Map.of(), resolvedIntent, inducedByInjection);
    }
}
