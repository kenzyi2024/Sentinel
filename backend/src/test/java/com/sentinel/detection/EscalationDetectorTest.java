package com.sentinel.detection;

import com.sentinel.domain.AgentAction;
import com.sentinel.domain.AttackPattern;
import com.sentinel.domain.Event;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.ResourceSensitivity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sentinel.support.TestData.event;
import static org.assertj.core.api.Assertions.assertThat;

class EscalationDetectorTest {

    private final EscalationDetector detector = new EscalationDetector(new CommandAnalyzer());

    @Test
    void scoresExfiltrationFollowingReconAndDiscoveryAtTheTop() {
        List<Event> history = List.of(
                event(1, ActionType.LIST_DIRECTORY, ResourceSensitivity.PUBLIC),
                event(2, ActionType.READ_FILE, ResourceSensitivity.SECRET));
        EscalationSignal signal = detector.analyze(history,
                AgentAction.network("evil.example", "send data"), ResourceSensitivity.PUBLIC);

        assertThat(signal.signal()).isGreaterThanOrEqualTo(0.85);
        assertThat(signal.reasons()).isNotEmpty();
    }

    @Test
    void isolatedBenignReadHasNoEscalationSignal() {
        List<Event> history = List.of(event(1, ActionType.READ_FILE, ResourceSensitivity.INTERNAL));
        EscalationSignal signal = detector.analyze(history,
                AgentAction.read("src/App.java", "code", "read"), ResourceSensitivity.INTERNAL);

        assertThat(signal.signal()).isZero();
    }

    @Test
    void detectsTheReconDiscoveryExfiltrationChainInAReport() {
        List<Event> events = List.of(
                event(1, ActionType.LIST_DIRECTORY, ResourceSensitivity.PUBLIC),
                event(2, ActionType.READ_ENV, ResourceSensitivity.SECRET),
                event(3, ActionType.NETWORK_REQUEST, ResourceSensitivity.PUBLIC));

        List<AttackPattern> patterns = detector.detectPatterns(events);
        assertThat(patterns).anyMatch(p -> p.id().equals("chain.recon-discovery-exfil"));
    }
}
