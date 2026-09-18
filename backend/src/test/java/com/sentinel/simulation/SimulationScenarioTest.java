package com.sentinel.simulation;

import com.sentinel.api.dto.RunDetail;
import com.sentinel.domain.Event;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.AgentState;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.RunStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end validation of the built-in scenarios: each is run through the full pipeline and its
 * key security outcomes are asserted. This is the test that proves the engine behaves as designed.
 */
@SpringBootTest
@ActiveProfiles("test")
class SimulationScenarioTest {

    @Autowired
    private SimulationEngine simulationEngine;

    @Test
    void normalDevelopmentStaysLowRiskWithNoBlocks() {
        RunDetail run = simulationEngine.run("normal-development");

        assertThat(run.summary().status()).isEqualTo(RunStatus.COMPLETED);
        assertThat(run.summary().blocked()).isZero();
        assertThat(run.events()).noneMatch(e -> e.decision() == Decision.BLOCKED);
    }

    @Test
    void secretExposureBlocksEverySecretRead() {
        RunDetail run = simulationEngine.run("secret-exposure");

        assertThat(run.events())
                .filteredOn(e -> e.resource().contains(".env") || e.resource().contains("credentials"))
                .isNotEmpty()
                .allMatch(e -> e.decision() == Decision.BLOCKED);
        assertThat(run.summary().blocked()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void compromisedAgentIsDetectedAndItsAttackChainBlocked() {
        RunDetail run = simulationEngine.run("compromised-agent");

        // The poisoned ticket is detected as injection...
        assertThat(run.events()).anyMatch(Event::hasInjection);
        // ...the agent transitions to COMPROMISED...
        assertThat(run.events()).anyMatch(e -> e.agentStateAfter() == AgentState.COMPROMISED);
        // ...the bulk enumeration is flagged as anomalous...
        assertThat(run.events()).anyMatch(e -> e.anomalyResult().enumerationDetected());
        // ...and the exfiltration attempt is blocked.
        assertThat(run.events())
                .filteredOn(e -> e.actionType() == ActionType.NETWORK_REQUEST)
                .isNotEmpty()
                .allMatch(e -> e.decision() == Decision.BLOCKED);

        assertThat(run.report().injectionFindings()).isGreaterThan(0);
        assertThat(run.report().detectedAttackPatterns()).isNotEmpty();
    }

    @Test
    void promptInjectionBlocksTheInducedSecretReadAndExfiltration() {
        RunDetail run = simulationEngine.run("prompt-injection");

        assertThat(run.events()).anyMatch(Event::hasInjection);
        assertThat(run.events())
                .filteredOn(e -> e.actionType() == ActionType.NETWORK_REQUEST
                        || e.actionType() == ActionType.READ_ENV
                        || e.resource().contains(".env"))
                .allMatch(e -> e.decision() == Decision.BLOCKED);
    }

    @Test
    void privilegeEscalationBlocksDangerousCommandsAndSensitiveDeletes() {
        RunDetail run = simulationEngine.run("privilege-escalation");

        assertThat(run.events())
                .filteredOn(e -> e.actionType() == ActionType.EXECUTE_COMMAND)
                .anyMatch(e -> e.decision() == Decision.BLOCKED);
        assertThat(run.events())
                .filteredOn(e -> e.actionType() == ActionType.DELETE_FILE)
                .allMatch(e -> e.decision() == Decision.BLOCKED);
    }

    @Test
    void abnormalFileAccessTripsTheAnomalyDetector() {
        RunDetail run = simulationEngine.run("abnormal-file-access");

        assertThat(run.events()).anyMatch(e -> e.anomalyResult().enumerationDetected());
        assertThat(run.report().behavioralAnomalies()).isNotEmpty();
    }

    @Test
    void everyActionProducesASequentiallyLinkedEvent() {
        List<Event> events = simulationEngine.run("compromised-agent").events();

        assertThat(events).isNotEmpty();
        assertThat(events.get(0).previousEventId()).isNull();
        for (int i = 0; i < events.size(); i++) {
            assertThat(events.get(i).sequence()).isEqualTo(i + 1);
            if (i > 0) {
                assertThat(events.get(i).previousEventId()).isEqualTo(events.get(i - 1).id());
            }
        }
    }
}
