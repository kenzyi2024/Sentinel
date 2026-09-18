package com.sentinel.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.domain.Event;
import com.sentinel.domain.InjectionFinding;
import com.sentinel.domain.RiskAssessment;
import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.InjectionCategory;
import com.sentinel.domain.enums.RiskBand;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.domain.enums.ResourceSensitivity;
import com.sentinel.domain.enums.Severity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final EventMapper mapper = new EventMapper(objectMapper);

    @Test
    void roundTripsTheRichEventThroughJson() {
        Event event = Event.builder()
                .id("e1").runId("run-1").sequence(1)
                .timestamp(Instant.parse("2026-01-01T00:00:00Z"))
                .agentId("agent").agentName("Agent")
                .actionType(ActionType.READ_FILE).resource(".env")
                .resourceSensitivity(ResourceSensitivity.SECRET)
                .riskAssessment(new RiskAssessment(63, RiskBand.HIGH,
                        List.of(RiskFactor.of(RiskCategory.PERMISSION, "Missing capability", 0.9, 40, "missing READ_SECRETS"))))
                .injectionFindings(List.of(new InjectionFinding("override.ignore-previous",
                        InjectionCategory.INSTRUCTION_OVERRIDE, Severity.CRITICAL, "ignore previous", 0, "why")))
                .decision(Decision.BLOCKED)
                .reasons(List.of("BLOCKED"))
                .build();

        EventEntity entity = mapper.toEntity(event);
        assertThat(entity.getRiskScore()).isEqualTo(63);
        assertThat(entity.getDecision()).isEqualTo(Decision.BLOCKED);
        assertThat(entity.getRiskBand()).isEqualTo(RiskBand.HIGH);

        Event restored = mapper.toDomain(entity);
        assertThat(restored.id()).isEqualTo("e1");
        assertThat(restored.riskAssessment().score()).isEqualTo(63);
        assertThat(restored.riskAssessment().factors()).hasSize(1);
        assertThat(restored.injectionFindings()).hasSize(1);
        assertThat(restored.injectionFindings().get(0).category()).isEqualTo(InjectionCategory.INSTRUCTION_OVERRIDE);
        assertThat(restored.timestamp()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    }
}
