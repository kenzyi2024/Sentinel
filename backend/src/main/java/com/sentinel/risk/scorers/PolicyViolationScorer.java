package com.sentinel.risk.scorers;

import com.sentinel.config.RiskModelProperties;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.evaluation.EvaluationContext;
import com.sentinel.risk.RiskScorer;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Turns a restrictive policy match into a risk contribution. A DENY carries the full signal; a
 * REQUIRE_APPROVAL and a WARN carry progressively less. Permissive effects (ALLOW / LOG_ONLY) add
 * no risk. This is what lets an explicit policy decision and the numeric risk score reinforce each
 * other rather than diverge.
 */
@Component
public class PolicyViolationScorer implements RiskScorer {

    private final RiskModelProperties properties;

    public PolicyViolationScorer(RiskModelProperties properties) {
        this.properties = properties;
    }

    @Override
    public RiskCategory category() {
        return RiskCategory.POLICY_VIOLATION;
    }

    @Override
    public Optional<RiskFactor> score(EvaluationContext context) {
        var decision = context.policyDecision();
        if (!decision.matched()) {
            return Optional.empty();
        }
        double signal = switch (decision.effect()) {
            case DENY -> 1.0;
            case REQUIRE_APPROVAL -> 0.5;
            case WARN -> 0.25;
            case ALLOW, LOG_ONLY -> 0.0;
        };
        if (signal <= 0.0) {
            return Optional.empty();
        }
        double weight = properties.getWeights().getPolicyViolation();
        return Optional.of(RiskFactor.of(category(), "Policy " + decision.effect(), signal, weight,
                "Matched policy '" + decision.policyName() + "': " + decision.reason()));
    }
}
