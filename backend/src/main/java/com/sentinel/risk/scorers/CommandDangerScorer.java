package com.sentinel.risk.scorers;

import com.sentinel.config.RiskModelProperties;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.evaluation.EvaluationContext;
import com.sentinel.risk.RiskScorer;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Scores risk from dangerous shell-command constructs, using the danger value already computed by
 * the {@code CommandAnalyzer}. Non-command actions contribute nothing.
 */
@Component
public class CommandDangerScorer implements RiskScorer {

    private final RiskModelProperties properties;

    public CommandDangerScorer(RiskModelProperties properties) {
        this.properties = properties;
    }

    @Override
    public RiskCategory category() {
        return RiskCategory.COMMAND_DANGER;
    }

    @Override
    public Optional<RiskFactor> score(EvaluationContext context) {
        var analysis = context.commandAnalysis();
        if (analysis == null || analysis.danger() <= 0.0) {
            return Optional.empty();
        }
        double weight = properties.getWeights().getCommandDanger();
        String reason = analysis.reasons().isEmpty()
                ? "Command flagged as potentially dangerous."
                : String.join(" ", analysis.reasons());
        return Optional.of(RiskFactor.of(category(), "Command danger", analysis.danger(), weight, reason));
    }
}
