package com.sentinel.simulation;

import com.sentinel.api.dto.RunDetail;
import com.sentinel.api.dto.RunSummary;
import com.sentinel.domain.Event;
import com.sentinel.domain.SecurityReport;
import com.sentinel.exception.ResourceNotFoundException;
import com.sentinel.persistence.RunEntity;
import com.sentinel.persistence.RunMapper;
import com.sentinel.repository.RunRepository;
import com.sentinel.telemetry.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-side service for runs: listing, detail assembly (summary + events + report), and lookups.
 * The simulation engine writes; this service reads back for the API.
 */
@Service
public class RunService {

    private final RunRepository runRepository;
    private final EventService eventService;
    private final RunMapper runMapper;

    public RunService(RunRepository runRepository, EventService eventService, RunMapper runMapper) {
        this.runRepository = runRepository;
        this.eventService = eventService;
        this.runMapper = runMapper;
    }

    @Transactional(readOnly = true)
    public List<RunSummary> list() {
        return runRepository.findAllByOrderByStartedAtDesc().stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public RunDetail detail(String id) {
        RunEntity entity = find(id);
        return new RunDetail(toSummary(entity), eventService.findByRun(id), runMapper.report(entity));
    }

    @Transactional(readOnly = true)
    public List<Event> events(String id) {
        find(id);
        return eventService.findByRun(id);
    }

    @Transactional(readOnly = true)
    public SecurityReport report(String id) {
        return runMapper.report(find(id));
    }

    @Transactional(readOnly = true)
    public long count() {
        return runRepository.count();
    }

    private RunEntity find(String id) {
        return runRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Run", id));
    }

    RunSummary toSummary(RunEntity entity) {
        return new RunSummary(entity.getId(), entity.getScenarioId(), entity.getScenarioName(),
                entity.getAgentName(), entity.getArchetype(), entity.getStatus(), entity.getStartedAt(),
                entity.getFinishedAt(), entity.getTotalActions(), entity.getAllowed(), entity.getBlocked(),
                entity.getRequiresApproval(), entity.getMaxRiskScore(), entity.getOverallRiskBand());
    }
}
