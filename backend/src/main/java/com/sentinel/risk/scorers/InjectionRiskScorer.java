package com.sentinel.risk.scorers;

import com.sentinel.config.RiskModelProperties;
import com.sentinel.domain.InjectionFinding;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.domain.enums.Severity;
import com.sentinel.evaluation.EvaluationContext;
import com.sentinel.risk.RiskScorer;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Scores risk from prompt-injection findings in the ingested content. Findings are combined with a
 * probabilistic OR of their normalized severities, so a single critical finding, or several
 * medium ones, both drive the signal high without any one finding being able to exceed 1.
 */
@Component
public class InjectionRiskScorer implements RiskScorer {

    private final RiskModelProperties properties;

    public InjectionRiskScorer(RiskModelProperties properties) {
        this.properties = properties;
    }

    @Override
    public RiskCategory category() {
        return RiskCategory.PROMPT_INJECTION;
    }

    @Override
    public Optional<RiskFactor> score(EvaluationContext context) {
        List<InjectionFinding> findings = context.injectionFindings();
        if (findings.isEmpty()) {
            return Optional.empty();
        }
        double survival = 1.0;
        for (InjectionFinding finding : findings) {
            double severityNorm = finding.severity().getWeight() / (double) Severity.maxWeight();
            survival *= (1.0 - severityNorm);
        }
        double signal = 1.0 - survival;

        InjectionFinding top = findings.stream()
                .max(Comparator.comparingInt(f -> f.severity().getWeight()))
                .orElse(findings.get(0));
        double weight = properties.getWeights().getPromptInjection();
        String explanation = findings.size() + " prompt-injection signal(s) in ingested content; "
                + "highest is " + top.severity() + " [" + top.category() + "] — " + top.explanation();
        return Optional.of(RiskFactor.of(category(), "Prompt injection", signal, weight, explanation));
    }
}
