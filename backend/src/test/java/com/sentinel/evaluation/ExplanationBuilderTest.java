package com.sentinel.evaluation;

import com.sentinel.detection.CommandAnalysis;
import com.sentinel.detection.EscalationSignal;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.AnomalyResult;
import com.sentinel.domain.InjectionFinding;
import com.sentinel.domain.PermissionCheck;
import com.sentinel.domain.PolicyDecision;
import com.sentinel.domain.RiskAssessment;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.InjectionCategory;
import com.sentinel.domain.enums.RiskBand;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.domain.enums.ResourceSensitivity;
import com.sentinel.domain.enums.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.sentinel.support.TestData.agent;
import static org.assertj.core.api.Assertions.assertThat;

class ExplanationBuilderTest {

    private final ExplanationBuilder builder = new ExplanationBuilder();

    @Test
    void blockedInjectionExplanationLeadsWithDecisionAndRecommendsTreatingDataAsUntrusted() {
        InjectionFinding finding = new InjectionFinding("override.ignore-previous",
                InjectionCategory.INSTRUCTION_OVERRIDE, Severity.CRITICAL, "ignore previous", 0, "override detected");
        EvaluationContext context = new EvaluationContext(
                agent(AgentArchetype.COMPROMISED, Capability.READ_PROJECT),
                AgentAction.read("issues/TICKET.md", "content", "read"),
                ResourceSensitivity.INTERNAL,
                Set.of(Capability.READ_PROJECT),
                new PermissionCheck(Set.of(Capability.READ_PROJECT), Set.of(Capability.READ_PROJECT), Set.of()),
                List.of(finding),
                CommandAnalysis.safe(), AnomalyResult.none(), EscalationSignal.none(),
                PolicyDecision.noMatch(), List.of());

        RiskAssessment assessment = new RiskAssessment(70, RiskBand.HIGH,
                List.of(RiskFactor.of(RiskCategory.PROMPT_INJECTION, "Prompt injection", 1.0, 30, "injection detected")));

        Explanation explanation = builder.explain(context, assessment, new Resolution(Decision.BLOCKED, false, List.of()));

        assertThat(explanation.reasons().get(0)).contains("BLOCKED");
        assertThat(explanation.reasons()).hasSizeGreaterThan(1);
        assertThat(explanation.recommendations()).anyMatch(r -> r.toLowerCase().contains("untrusted"));
    }
}
