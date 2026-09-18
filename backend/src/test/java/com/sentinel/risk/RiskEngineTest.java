package com.sentinel.risk;

import com.sentinel.config.RiskModelProperties;
import com.sentinel.detection.CommandAnalysis;
import com.sentinel.detection.EscalationSignal;
import com.sentinel.domain.Agent;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.AnomalyResult;
import com.sentinel.domain.PermissionCheck;
import com.sentinel.domain.PolicyDecision;
import com.sentinel.domain.RiskAssessment;
import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.RiskBand;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.domain.enums.ResourceSensitivity;
import com.sentinel.evaluation.EvaluationContext;
import com.sentinel.risk.scorers.BehavioralAnomalyScorer;
import com.sentinel.risk.scorers.CommandDangerScorer;
import com.sentinel.risk.scorers.InjectionRiskScorer;
import com.sentinel.risk.scorers.PermissionRiskScorer;
import com.sentinel.risk.scorers.PolicyViolationScorer;
import com.sentinel.risk.scorers.PrivilegeEscalationScorer;
import com.sentinel.risk.scorers.ResourceSensitivityScorer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.sentinel.support.TestData.agent;
import static org.assertj.core.api.Assertions.assertThat;

class RiskEngineTest {

    private final RiskModelProperties properties = new RiskModelProperties();
    private final RiskEngine engine = new RiskEngine(List.of(
            new PermissionRiskScorer(properties),
            new ResourceSensitivityScorer(properties),
            new CommandDangerScorer(properties),
            new InjectionRiskScorer(properties),
            new BehavioralAnomalyScorer(properties),
            new PolicyViolationScorer(properties),
            new PrivilegeEscalationScorer(properties)), properties);

    @Test
    void secretReadWithoutClearanceScoresHighWithExplainableFactors() {
        Agent research = agent(AgentArchetype.RESEARCH, Capability.READ_PROJECT);
        AgentAction readEnv = AgentAction.read(".env", "secret", "read env");
        PermissionCheck missing = new PermissionCheck(
                Set.of(Capability.READ_PROJECT, Capability.READ_SECRETS),
                Set.of(Capability.READ_PROJECT),
                Set.of(Capability.READ_SECRETS));

        EvaluationContext context = new EvaluationContext(research, readEnv, ResourceSensitivity.SECRET,
                Set.of(Capability.READ_PROJECT, Capability.READ_SECRETS), missing, List.of(),
                CommandAnalysis.safe(), AnomalyResult.none(), EscalationSignal.none(),
                PolicyDecision.noMatch(), List.of());

        RiskAssessment assessment = engine.assess(context);

        assertThat(assessment.score()).isGreaterThanOrEqualTo(60);
        assertThat(assessment.band()).isIn(RiskBand.HIGH, RiskBand.CRITICAL);
        assertThat(assessment.factors())
                .extracting(f -> f.category())
                .contains(RiskCategory.PERMISSION, RiskCategory.RESOURCE_SENSITIVITY);
        // The sum of the factor contributions reconciles with the total score.
        int sum = assessment.factors().stream().mapToInt(f -> f.contribution()).sum();
        assertThat(Math.min(100, sum)).isEqualTo(assessment.score());
    }

    @Test
    void benignInternalReadScoresLow() {
        Agent developer = agent(AgentArchetype.DEVELOPER, Capability.READ_PROJECT);
        AgentAction read = AgentAction.read("src/App.java", "code", "read");
        PermissionCheck ok = new PermissionCheck(Set.of(Capability.READ_PROJECT),
                Set.of(Capability.READ_PROJECT), Set.of());

        EvaluationContext context = new EvaluationContext(developer, read, ResourceSensitivity.INTERNAL,
                Set.of(Capability.READ_PROJECT), ok, List.of(), CommandAnalysis.safe(),
                AnomalyResult.none(), EscalationSignal.none(), PolicyDecision.noMatch(), List.of());

        RiskAssessment assessment = engine.assess(context);
        assertThat(assessment.band()).isEqualTo(RiskBand.LOW);
    }
}
