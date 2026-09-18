package com.sentinel.domain;

import java.util.List;

/**
 * Output of the behavioral anomaly detector for a single action, given the agent's recent
 * history. The score is normalized to [0, 1]; the named signals and reasons make the score
 * explainable ("read rate 8x baseline", "first sensitive-resource access after bulk enumeration").
 *
 * @param score                     normalized anomaly score in [0, 1]
 * @param zScore                    standard deviations of the current action rate above the moving baseline
 * @param windowActionCount         number of actions in the current sliding window
 * @param enumerationDetected       whether the window shows bulk file enumeration
 * @param sensitiveAfterEnumeration whether a sensitive resource was touched right after enumeration
 * @param reasons                   human-readable explanations for the score
 */
public record AnomalyResult(
        double score,
        double zScore,
        int windowActionCount,
        boolean enumerationDetected,
        boolean sensitiveAfterEnumeration,
        List<String> reasons
) {
    public AnomalyResult {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static AnomalyResult none() {
        return new AnomalyResult(0.0, 0.0, 0, false, false, List.of());
    }
}
