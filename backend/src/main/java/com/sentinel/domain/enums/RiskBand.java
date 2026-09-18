package com.sentinel.domain.enums;

/**
 * A qualitative bucket for a numeric risk score in [0, 100]. Cut-points are configurable
 * (see {@code sentinel.risk.bands}); the no-argument overload uses the documented defaults so
 * that unit tests and callers without configuration behave predictably.
 */
public enum RiskBand {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static RiskBand fromScore(int score, int mediumCut, int highCut, int criticalCut) {
        if (score >= criticalCut) {
            return CRITICAL;
        }
        if (score >= highCut) {
            return HIGH;
        }
        if (score >= mediumCut) {
            return MEDIUM;
        }
        return LOW;
    }

    /** Convenience overload using the default cut-points (30 / 60 / 85). */
    public static RiskBand fromScore(int score) {
        return fromScore(score, 30, 60, 85);
    }
}
