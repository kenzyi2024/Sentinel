package com.sentinel.risk.scorers;

import com.sentinel.config.RiskModelProperties;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.evaluation.EvaluationContext;
import com.sentinel.risk.RiskScorer;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scores the gap between the authority an action needs and the authority the agent holds. A missing
 * capability is a least-privilege violation and the dominant signal here; even when permissions are
 * satisfied, exercising an inherently sensitive capability carries a small baseline.
 */
@Component
public class PermissionRiskScorer implements RiskScorer {

    private static final Set<Capability> SENSITIVE_CAPABILITIES = EnumSet.of(
            Capability.READ_SECRETS, Capability.EXECUTE_COMMAND, Capability.NETWORK_ACCESS,
            Capability.DELETE_FILE, Capability.MODIFY_CONFIG);

    private final RiskModelProperties properties;

    public PermissionRiskScorer(RiskModelProperties properties) {
        this.properties = properties;
    }

    @Override
    public RiskCategory category() {
        return RiskCategory.PERMISSION;
    }

    @Override
    public Optional<RiskFactor> score(EvaluationContext context) {
        double weight = properties.getWeights().getPermission();
        var check = context.permissionCheck();

        if (!check.satisfied()) {
            int maxPrivilege = check.missing().stream()
                    .mapToInt(Capability::getPrivilegeWeight)
                    .max()
                    .orElse(1);
            double signal = 0.45 + (maxPrivilege / 10.0) * 0.55;
            String missing = check.missing().stream().map(Enum::name).collect(Collectors.joining(", "));
            return Optional.of(RiskFactor.of(category(), "Missing capability", signal, weight,
                    "Agent lacks required capability [" + missing + "]. The action exceeds its granted "
                            + "authority — a least-privilege violation."));
        }

        var sensitiveUsed = context.requiredCapabilities().stream()
                .filter(SENSITIVE_CAPABILITIES::contains)
                .map(Enum::name)
                .collect(Collectors.joining(", "));
        if (!sensitiveUsed.isEmpty()) {
            return Optional.of(RiskFactor.of(category(), "Sensitive capability exercised", 0.15, weight,
                    "Action exercises a sensitive capability the agent holds [" + sensitiveUsed + "]."));
        }
        return Optional.empty();
    }
}
