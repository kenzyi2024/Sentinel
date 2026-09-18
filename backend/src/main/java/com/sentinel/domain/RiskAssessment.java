package com.sentinel.domain;

import com.sentinel.domain.enums.RiskBand;

import java.util.Comparator;
import java.util.List;

/**
 * The result of scoring one action: a total score in [0, 100], its qualitative band, and the
 * ordered list of {@link RiskFactor factors} that produced it. Holding the factors (not just the
 * number) is what makes every decision explainable after the fact.
 */
public record RiskAssessment(
        int score,
        RiskBand band,
        List<RiskFactor> factors
) {
    public RiskAssessment {
        factors = factors == null ? List.of() : List.copyOf(factors);
    }

    /** Factors sorted by contribution, largest first — the "top reasons" for the score. */
    public List<RiskFactor> topFactors() {
        return factors.stream()
                .sorted(Comparator.comparingInt(RiskFactor::contribution).reversed())
                .toList();
    }

    public static RiskAssessment none() {
        return new RiskAssessment(0, RiskBand.LOW, List.of());
    }
}
