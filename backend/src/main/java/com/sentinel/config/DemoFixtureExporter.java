package com.sentinel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.api.dto.RunDetail;
import com.sentinel.policy.PolicyService;
import com.sentinel.simulation.AgentFactory;
import com.sentinel.simulation.ScenarioDefinition;
import com.sentinel.simulation.ScenarioLibrary;
import com.sentinel.simulation.SimulationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exports genuine engine output as JSON fixtures for the static (GitHub Pages) frontend, then exits.
 * Every fixture is produced by running the real scenarios through the real pipeline — the demo data
 * is not hand-written, so the deployed demo shows exactly what the backend would.
 *
 * <p>Run with: {@code ./mvnw spring-boot:run -Dspring-boot.run.profiles=export}
 * (optionally {@code -Dspring-boot.run.arguments=--sentinel.export.dir=...}).
 */
@Component
@Profile("export")
@Order(2)
public class DemoFixtureExporter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoFixtureExporter.class);

    private final SimulationEngine simulationEngine;
    private final ScenarioLibrary scenarioLibrary;
    private final AgentFactory agentFactory;
    private final PolicyService policyService;
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;
    private final String exportDir;

    public DemoFixtureExporter(SimulationEngine simulationEngine, ScenarioLibrary scenarioLibrary,
                               AgentFactory agentFactory, PolicyService policyService, ObjectMapper objectMapper,
                               ApplicationContext applicationContext,
                               @Value("${sentinel.export.dir:../frontend/src/demo/fixtures}") String exportDir) {
        this.simulationEngine = simulationEngine;
        this.scenarioLibrary = scenarioLibrary;
        this.agentFactory = agentFactory;
        this.policyService = policyService;
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
        this.exportDir = exportDir;
    }

    @Override
    public void run(String... args) throws Exception {
        Path base = Path.of(exportDir);
        Path runsDir = base.resolve("runs");
        Files.createDirectories(runsDir);

        write(base.resolve("scenarios.json"), scenarioLibrary.infos());
        write(base.resolve("agents.json"), agentFactory.all());
        write(base.resolve("policies.json"), policyService.findAll());

        Map<String, String> runIndex = new LinkedHashMap<>();
        for (ScenarioDefinition definition : scenarioLibrary.definitions()) {
            String id = definition.info().id();
            RunDetail detail = simulationEngine.execute(definition);
            write(runsDir.resolve(id + ".json"), detail);
            runIndex.put(id, detail.summary().id());
            log.info("Exported run for scenario '{}' ({} events).", id, detail.events().size());
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("generatedAt", Instant.now().toString());
        meta.put("scenarios", runIndex.keySet());
        write(base.resolve("index.json"), meta);

        log.info("Demo fixtures written to {}", base.toAbsolutePath());
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    private void write(Path path, Object value) throws Exception {
        File file = path.toFile();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, value);
    }
}
