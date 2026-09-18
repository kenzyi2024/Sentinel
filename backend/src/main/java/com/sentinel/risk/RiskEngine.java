package com.sentinel.risk;

import com.sentinel.config.RiskModelProperties;
import com.sentinel.domain.RiskAssessment;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.RiskBand;
import com.sentinel.evaluation.EvaluationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates every {@link RiskScorer} into a single explainable {@link RiskAssessment}.
 *
 * <p>The model is an additive weighted sum: each scorer contributes {@code round(signal × weight)}
 * points, the points are summed and clamped to [0, 100], and the total is bucketed into a
 * {@link RiskBand}. A weighted sum (rather than the multiplicative chain the brief sketched) is a
 * deliberate choice — it is monotonic, every factor's contribution is independently legible, and
 * one zeroed signal can never wipe out the others. Because Spring injects the full list of scorer
 * beans, the set of dimensions is open for extension without touching this class.
 */
@Component
public class RiskEngine {

    private final List<RiskScorer> scorers;
    private final RiskModelProperties properties;

    public RiskEngine(List<RiskScorer> scorers, RiskModelProperties properties) {
        this.scorers = scorers;
        this.properties = properties;
    }

    public RiskAssessment assess(EvaluationContext context) {
        List<RiskFactor> factors = new ArrayList<>();
        for (RiskScorer scorer : scorers) {
            scorer.score(context).ifPresent(factors::add);
        }
        int total = factors.stream().mapToInt(RiskFactor::contribution).sum();
        int score = Math.max(0, Math.min(100, total));
        RiskBand band = properties.band(score);
        return new RiskAssessment(score, band, factors);
    }

    /** The number of registered scoring dimensions — surfaced in the API's engine metadata. */
    public int scorerCount() {
        return scorers.size();
    }
}
