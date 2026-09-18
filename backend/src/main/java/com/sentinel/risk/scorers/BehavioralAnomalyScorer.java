package com.sentinel.risk.scorers;

import com.sentinel.config.RiskModelProperties;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.evaluation.EvaluationContext;
import com.sentinel.risk.RiskScorer;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Scores risk from the behavioral anomaly detector's output (bulk enumeration, frequency spikes,
 * sensitive-after-enumeration). The anomaly score is already normalized, so it maps directly onto
 * the signal.
 */
@Component
public class BehavioralAnomalyScorer implements RiskScorer {

    private final RiskModelProperties properties;

    public BehavioralAnomalyScorer(RiskModelProperties properties) {
        this.properties = properties;
    }

    @Override
    public RiskCategory category() {
        return RiskCategory.BEHAVIORAL_ANOMALY;
    }

    @Override
    public Optional<RiskFactor> score(EvaluationContext context) {
        var anomaly = context.anomalyResult();
        if (anomaly == null || anomaly.score() <= 0.0) {
            return Optional.empty();
        }
        double weight = properties.getWeights().getBehavioralAnomaly();
        String explanation = anomaly.reasons().isEmpty()
                ? "Behavior deviates from the agent's established baseline."
                : String.join(" ", anomaly.reasons());
        return Optional.of(RiskFactor.of(category(), "Behavioral anomaly", anomaly.score(), weight, explanation));
    }
}
