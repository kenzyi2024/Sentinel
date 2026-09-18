package com.sentinel.detection;

import java.util.List;

/**
 * The result of inspecting a shell command for danger.
 *
 * @param danger        normalized danger in [0, 1]
 * @param reasons       human-readable explanations for each dangerous pattern matched
 * @param exfiltration  whether the command looks like data exfiltration
 * @param escalation    whether the command looks like privilege escalation / persistence
 */
public record CommandAnalysis(
        double danger,
        List<String> reasons,
        boolean exfiltration,
        boolean escalation
) {
    public CommandAnalysis {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static CommandAnalysis safe() {
        return new CommandAnalysis(0.0, List.of(), false, false);
    }
}
