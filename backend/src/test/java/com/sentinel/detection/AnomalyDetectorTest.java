package com.sentinel.detection;

import com.sentinel.config.AnomalyProperties;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.AnomalyResult;
import com.sentinel.domain.Event;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.ResourceSensitivity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.sentinel.support.TestData.event;
import static org.assertj.core.api.Assertions.assertThat;

class AnomalyDetectorTest {

    private final AnomalyDetector detector = new AnomalyDetector(new AnomalyProperties());

    @Test
    void quietActivityIsNotAnomalous() {
        List<Event> history = List.of(
                event(1, ActionType.READ_FILE),
                event(2, ActionType.EXECUTE_COMMAND),
                event(3, ActionType.READ_FILE));
        AnomalyResult result = detector.analyze(history,
                AgentAction.read("src/App.java", "code", "read"), ResourceSensitivity.INTERNAL);

        assertThat(result.enumerationDetected()).isFalse();
        assertThat(result.score()).isZero();
    }

    @Test
    void bulkEnumerationIsFlagged() {
        List<Event> history = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            history.add(event(i, ActionType.READ_FILE));
        }
        AnomalyResult result = detector.analyze(history,
                AgentAction.read("src/File16.java", "", "read"), ResourceSensitivity.INTERNAL);

        assertThat(result.enumerationDetected()).isTrue();
        assertThat(result.score()).isGreaterThan(0.5);
        assertThat(result.reasons()).isNotEmpty();
    }

    @Test
    void sensitiveAccessAfterEnumerationCompoundsTheScore() {
        List<Event> history = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            history.add(event(i, ActionType.READ_FILE));
        }
        AnomalyResult result = detector.analyze(history,
                AgentAction.read(".env", "secret", "read env"), ResourceSensitivity.SECRET);

        assertThat(result.sensitiveAfterEnumeration()).isTrue();
        assertThat(result.score()).isGreaterThan(0.7);
    }
}
