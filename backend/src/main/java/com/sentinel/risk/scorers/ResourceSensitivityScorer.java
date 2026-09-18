package com.sentinel.risk.scorers;

import com.sentinel.config.RiskModelProperties;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.evaluation.EvaluationContext;
import com.sentinel.risk.RiskScorer;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Scores risk from the sensitivity of the targeted resource. A public file contributes nothing; a
 * secret contributes the full weight. The signal scales linearly across the four sensitivity tiers.
 */
@Component
public class ResourceSensitivityScorer implements RiskScorer {

    private final RiskModelProperties properties;

    public ResourceSensitivityScorer(RiskModelProperties properties) {
        this.properties = properties;
    }

    @Override
    public RiskCategory category() {
        return RiskCategory.RESOURCE_SENSITIVITY;
    }

    @Override
    public Optional<RiskFactor> score(EvaluationContext context) {
        var sensitivity = context.sensitivity();
        // PUBLIC=1..SECRET=4 mapped to a 0..1 signal.
        double signal = (sensitivity.getWeight() - 1) / 3.0;
        if (signal <= 0.0) {
            return Optional.empty();
        }
        double weight = properties.getWeights().getResourceSensitivity();
        return Optional.of(RiskFactor.of(category(), "Resource sensitivity", signal, weight,
                "Target '" + context.action().resource() + "' is classified " + sensitivity.name()
                        + " (" + sensitivity.getDescription() + ")."));
    }
}
