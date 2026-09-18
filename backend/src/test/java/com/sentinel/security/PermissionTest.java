package com.sentinel.security;

import com.sentinel.domain.Agent;
import com.sentinel.domain.PermissionCheck;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.ResourceSensitivity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionTest {

    private final PermissionModel model = new PermissionModel();
    private final PermissionChecker checker = new PermissionChecker();

    @Test
    void readingAnOrdinaryFileNeedsOnlyReadProject() {
        assertThat(model.requiredCapabilities(ActionType.READ_FILE, ResourceSensitivity.INTERNAL))
                .containsExactly(Capability.READ_PROJECT);
    }

    @Test
    void readingASecretFileAlsoNeedsReadSecrets() {
        assertThat(model.requiredCapabilities(ActionType.READ_FILE, ResourceSensitivity.SECRET))
                .contains(Capability.READ_PROJECT, Capability.READ_SECRETS);
    }

    @Test
    void actionTypesMapToTheirCapabilities() {
        assertThat(model.requiredCapabilities(ActionType.READ_ENV, ResourceSensitivity.SECRET))
                .containsExactly(Capability.READ_SECRETS);
        assertThat(model.requiredCapabilities(ActionType.EXECUTE_COMMAND, ResourceSensitivity.PUBLIC))
                .containsExactly(Capability.EXECUTE_COMMAND);
        assertThat(model.requiredCapabilities(ActionType.NETWORK_REQUEST, ResourceSensitivity.PUBLIC))
                .containsExactly(Capability.NETWORK_ACCESS);
    }

    @Test
    void checkerReportsMissingCapabilities() {
        Agent research = new Agent("r", "Research", AgentArchetype.RESEARCH, "goal", Set.of(Capability.READ_PROJECT));
        PermissionCheck check = checker.check(research,
                Set.of(Capability.READ_PROJECT, Capability.READ_SECRETS));

        assertThat(check.satisfied()).isFalse();
        assertThat(check.missing()).containsExactly(Capability.READ_SECRETS);
    }

    @Test
    void checkerPassesWhenAllCapabilitiesGranted() {
        Agent dev = new Agent("d", "Dev", AgentArchetype.DEVELOPER, "goal",
                Set.of(Capability.READ_PROJECT, Capability.EXECUTE_COMMAND));
        PermissionCheck check = checker.check(dev, Set.of(Capability.EXECUTE_COMMAND));

        assertThat(check.satisfied()).isTrue();
        assertThat(check.missing()).isEmpty();
    }
}
