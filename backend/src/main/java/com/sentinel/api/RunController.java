package com.sentinel.api;

import com.sentinel.api.dto.RunDetail;
import com.sentinel.api.dto.RunRequest;
import com.sentinel.api.dto.RunSummary;
import com.sentinel.domain.Event;
import com.sentinel.domain.SecurityReport;
import com.sentinel.simulation.RunService;
import com.sentinel.simulation.SimulationEngine;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API for launching simulation runs and reading back their events and reports.
 */
@RestController
@RequestMapping("/api/runs")
public class RunController {

    private final SimulationEngine simulationEngine;
    private final RunService runService;

    public RunController(SimulationEngine simulationEngine, RunService runService) {
        this.simulationEngine = simulationEngine;
        this.runService = runService;
    }

    /** Launch a scenario and return the full evaluated run. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunDetail create(@Valid @RequestBody RunRequest request) {
        return simulationEngine.run(request.scenarioId());
    }

    @GetMapping
    public List<RunSummary> list() {
        return runService.list();
    }

    @GetMapping("/{id}")
    public RunDetail get(@PathVariable String id) {
        return runService.detail(id);
    }

    @GetMapping("/{id}/events")
    public List<Event> events(@PathVariable String id) {
        return runService.events(id);
    }

    @GetMapping("/{id}/report")
    public SecurityReport report(@PathVariable String id) {
        return runService.report(id);
    }
}
