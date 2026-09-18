package com.sentinel.evaluation;

import com.sentinel.domain.PermissionCheck;
import com.sentinel.domain.PolicyDecision;
import com.sentinel.domain.RiskAssessment;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.PolicyEffect;
import com.sentinel.domain.enums.RiskBand;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionResolverTest {

    private final DecisionResolver resolver = new DecisionResolver();

    private static final PermissionCheck SATISFIED = new PermissionCheck(Set.of(), Set.of(), Set.of());
    private static final PermissionCheck MISSING =
            new PermissionCheck(Set.of(Capability.READ_SECRETS), Set.of(), Set.of(Capability.READ_SECRETS));

    private static RiskAssessment risk(int score, RiskBand band) {
        return new RiskAssessment(score, band, List.of());
    }

    @Test
    void missingCapabilityIsAlwaysBlocked() {
        Resolution resolution = resolver.resolve(MISSING, PolicyDecision.noMatch(), risk(5, RiskBand.LOW), 0.0);
        assertThat(resolution.decision()).isEqualTo(Decision.BLOCKED);
        assertThat(resolution.executed()).isFalse();
    }

    @Test
    void neverAllowedCommandIsBlocked() {
        Resolution resolution = resolver.resolve(SATISFIED, PolicyDecision.noMatch(), risk(20, RiskBand.LOW), 0.95);
        assertThat(resolution.decision()).isEqualTo(Decision.BLOCKED);
    }

    @Test
    void explicitDenyAndCriticalRiskBlock() {
        PolicyDecision deny = new PolicyDecision(true, "p", "P", PolicyEffect.DENY, "denied");
        assertThat(resolver.resolve(SATISFIED, deny, risk(10, RiskBand.LOW), 0.0).decision())
                .isEqualTo(Decision.BLOCKED);
        assertThat(resolver.resolve(SATISFIED, PolicyDecision.noMatch(), risk(90, RiskBand.CRITICAL), 0.0).decision())
                .isEqualTo(Decision.BLOCKED);
    }

    @Test
    void approvalPolicyAndHighRiskRequireApproval() {
        PolicyDecision approval = new PolicyDecision(true, "p", "P", PolicyEffect.REQUIRE_APPROVAL, "review");
        assertThat(resolver.resolve(SATISFIED, approval, risk(20, RiskBand.LOW), 0.0).decision())
                .isEqualTo(Decision.REQUIRES_APPROVAL);
        assertThat(resolver.resolve(SATISFIED, PolicyDecision.noMatch(), risk(70, RiskBand.HIGH), 0.0).decision())
                .isEqualTo(Decision.REQUIRES_APPROVAL);
    }

    @Test
    void lowRiskIsAllowedAndMediumRiskWarns() {
        Resolution low = resolver.resolve(SATISFIED, PolicyDecision.noMatch(), risk(10, RiskBand.LOW), 0.0);
        assertThat(low.decision()).isEqualTo(Decision.ALLOWED);
        assertThat(low.executed()).isTrue();
        assertThat(low.warnings()).isEmpty();

        Resolution medium = resolver.resolve(SATISFIED, PolicyDecision.noMatch(), risk(40, RiskBand.MEDIUM), 0.0);
        assertThat(medium.decision()).isEqualTo(Decision.ALLOWED);
        assertThat(medium.warnings()).isNotEmpty();
    }
}
