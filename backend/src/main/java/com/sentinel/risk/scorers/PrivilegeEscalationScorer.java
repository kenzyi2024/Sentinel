package com.sentinel.risk.scorers;

import com.sentinel.config.RiskModelProperties;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.evaluation.EvaluationContext;
import com.sentinel.risk.RiskScorer;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Scores risk from multi-step escalation / attack-chain analysis. The escalation detector already
 * accounts for the ordering of prior actions (reconnaissance, discovery, exfiltration) and for
 * post-injection behavior; this scorer maps its signal onto the risk model. This is the dimension
 * that makes an action's risk depend on the <em>sequence</em> that preceded it, not just the action
 * in isolation.
 */
@Component
public class PrivilegeEscalationScorer implements RiskScorer {

    private final RiskModelProperties properties;

    public PrivilegeEscalationScorer(RiskModelProperties properties) {
        this.properties = properties;
    }

    @Override
    public RiskCategory category() {
        return RiskCategory.PRIVILEGE_ESCALATION;
    }

    @Override
    public Optional<RiskFactor> score(EvaluationContext context) {
        var escalation = context.escalationSignal();
        if (escalation == null || escalation.signal() <= 0.0) {
            return Optional.empty();
        }
        double weight = properties.getWeights().getPrivilegeEscalation();
        String explanation = escalation.reasons().isEmpty()
                ? "Action fits an escalation pattern given the run's history."
                : String.join(" ", escalation.reasons());
        return Optional.of(RiskFactor.of(category(), "Escalation pattern", escalation.signal(), weight, explanation));
    }
}
