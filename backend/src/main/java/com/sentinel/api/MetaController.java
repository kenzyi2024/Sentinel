package com.sentinel.api;

import com.sentinel.api.dto.EngineMeta;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.Capability;
import com.sentinel.risk.RiskEngine;
import com.sentinel.simulation.RunService;
import com.sentinel.simulation.ScenarioLibrary;
import com.sentinel.telemetry.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Engine metadata and a lightweight health endpoint. The frontend polls {@code /api/health} to
 * decide whether to run in live mode (backend reachable) or fall back to bundled demo data.
 */
@RestController
@RequestMapping("/api")
public class MetaController {

    private static final String VERSION = "0.1.0";

    private final RiskEngine riskEngine;
    private final ScenarioLibrary scenarioLibrary;
    private final RunService runService;
    private final EventService eventService;

    public MetaController(RiskEngine riskEngine, ScenarioLibrary scenarioLibrary,
                          RunService runService, EventService eventService) {
        this.riskEngine = riskEngine;
        this.scenarioLibrary = scenarioLibrary;
        this.runService = runService;
        this.eventService = eventService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "sentinel");
    }

    @GetMapping("/meta")
    public EngineMeta meta() {
        return new EngineMeta(
                "Sentinel",
                VERSION,
                true,
                false,
                riskEngine.scorerCount(),
                scenarioLibrary.infos().size(),
                runService.count(),
                eventService.totalEvents(),
                Arrays.stream(Capability.values()).map(Enum::name).toList(),
                Arrays.stream(ActionType.values()).map(Enum::name).toList());
    }
}
