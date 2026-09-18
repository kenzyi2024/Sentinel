package com.sentinel.api;

import com.sentinel.domain.ScenarioInfo;
import com.sentinel.exception.ResourceNotFoundException;
import com.sentinel.simulation.ScenarioDefinition;
import com.sentinel.simulation.ScenarioLibrary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only API for the built-in scenario catalog (the Scenario Lab).
 */
@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final ScenarioLibrary scenarioLibrary;

    public ScenarioController(ScenarioLibrary scenarioLibrary) {
        this.scenarioLibrary = scenarioLibrary;
    }

    @GetMapping
    public List<ScenarioInfo> list() {
        return scenarioLibrary.infos();
    }

    @GetMapping("/{id}")
    public ScenarioInfo get(@PathVariable String id) {
        return scenarioLibrary.byId(id)
                .map(ScenarioDefinition::info)
                .orElseThrow(() -> ResourceNotFoundException.of("Scenario", id));
    }
}
