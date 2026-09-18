package com.sentinel.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.domain.Agent;
import com.sentinel.domain.Event;
import com.sentinel.domain.ScenarioInfo;
import com.sentinel.domain.SecurityReport;
import com.sentinel.domain.enums.RunStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Builds a {@link RunEntity} from the output of a completed run (promoting report figures to
 * columns and storing the full report as JSON), and reconstructs the {@link SecurityReport} on read.
 */
@Component
public class RunMapper {

    private final ObjectMapper objectMapper;

    public RunMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RunEntity toEntity(String runId, ScenarioInfo scenario, Agent agent, RunStatus status,
                              Instant startedAt, Instant finishedAt, List<Event> events, SecurityReport report) {
        RunEntity entity = new RunEntity();
        entity.setId(runId);
        entity.setScenarioId(scenario.id());
        entity.setScenarioName(scenario.name());
        entity.setAgentId(agent.id());
        entity.setAgentName(agent.name());
        entity.setArchetype(agent.archetype());
        entity.setStatus(status);
        entity.setStartedAt(startedAt);
        entity.setFinishedAt(finishedAt);
        entity.setTotalActions(report.totalActions());
        entity.setAllowed(report.allowed());
        entity.setBlocked(report.blocked());
        entity.setRequiresApproval(report.requiresApproval());
        entity.setMaxRiskScore(report.maxRiskScore());
        entity.setOverallRiskBand(report.overallRiskBand());
        entity.setReportJson(write(report));
        return entity;
    }

    public SecurityReport report(RunEntity entity) {
        try {
            return objectMapper.readValue(entity.getReportJson(), SecurityReport.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize report for run " + entity.getId(), e);
        }
    }

    private String write(SecurityReport report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize report for run " + report.runId(), e);
        }
    }
}
