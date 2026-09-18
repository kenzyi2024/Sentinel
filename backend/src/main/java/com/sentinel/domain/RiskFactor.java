package com.sentinel.domain;

import com.sentinel.domain.enums.RiskCategory;

/**
 * A single, explainable contribution to an action's risk score. Every risk scorer emits factors
 * rather than opaque numbers: {@code contribution = round(signal * weight)}, and {@code explanation}
 * states in plain language why the signal fired. Summing the factors' contributions (then clamping
 * to [0, 100]) yields the total score, so the "why" always reconciles with the "how much".
 *
 * @param category     the risk dimension this factor belongs to
 * @param name         a short label for the factor
 * @param signal       the scorer's normalized signal in [0, 1]
 * @param weight       the configured weight applied to the signal
 * @param contribution points this factor adds to the total score
 * @param explanation  human-readable justification
 */
public record RiskFactor(
        RiskCategory category,
        String name,
        double signal,
        double weight,
        int contribution,
        String explanation
) {
    public static RiskFactor of(RiskCategory category, String name, double signal, double weight, String explanation) {
        double clamped = Math.max(0.0, Math.min(1.0, signal));
        int contribution = (int) Math.round(clamped * weight);
        return new RiskFactor(category, name, clamped, weight, contribution, explanation);
    }
}
