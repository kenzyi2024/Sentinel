package com.sentinel.simulation;

import com.sentinel.api.dto.CustomScenarioRequest;
import com.sentinel.api.dto.RunDetail;
import com.sentinel.config.RiskModelProperties;
import com.sentinel.domain.Agent;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.Event;
import com.sentinel.domain.ScenarioInfo;
import com.sentinel.domain.Policy;
import com.sentinel.domain.SecurityReport;
import com.sentinel.domain.enums.AgentState;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.RiskBand;
import com.sentinel.domain.enums.RunStatus;
import com.sentinel.evaluation.ActionEvaluator;
import com.sentinel.evaluation.EvaluationRequest;
import com.sentinel.exception.ResourceNotFoundException;
import com.sentinel.persistence.RunMapper;
import com.sentinel.policy.PolicyService;
import com.sentinel.report.ReportService;
import com.sentinel.repository.RunRepository;
import com.sentinel.telemetry.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Drives a scenario end to end: it feeds each scripted action through the {@link ActionEvaluator}
 * one at a time — passing the growing event history forward so anomaly and escalation detection can
 * see it — tracks the agent's state and a kill-switch counter, then persists the events and a
 * generated {@link SecurityReport}.
 *
 * <p>The kill switch terminates a run early once it has blocked
 * {@code sentinel.risk.critical-block-terminate-threshold} CRITICAL actions, modeling an
 * orchestrator that stops an agent which is clearly behaving maliciously.
 */
@Service
public class SimulationEngine {

    private static final long ACTION_SPACING_MS = 400;

    private final ScenarioLibrary scenarioLibrary;
    private final ActionEvaluator actionEvaluator;
    private final PolicyService policyService;
    private final EventService eventService;
    private final ReportService reportService;
    private final RunRepository runRepository;
    private final RunMapper runMapper;
    private final RunService runService;
    private final RiskModelProperties riskProperties;

    public SimulationEngine(ScenarioLibrary scenarioLibrary, ActionEvaluator actionEvaluator,
                            PolicyService policyService, EventService eventService, ReportService reportService,
                            RunRepository runRepository, RunMapper runMapper, RunService runService,
                            RiskModelProperties riskProperties) {
        this.scenarioLibrary = scenarioLibrary;
        this.actionEvaluator = actionEvaluator;
        this.policyService = policyService;
        this.eventService = eventService;
        this.reportService = reportService;
        this.runRepository = runRepository;
        this.runMapper = runMapper;
        this.runService = runService;
        this.riskProperties = riskProperties;
    }

    @Transactional
    public RunDetail run(String scenarioId) {
        ScenarioDefinition definition = scenarioLibrary.byId(scenarioId)
                .orElseThrow(() -> ResourceNotFoundException.of("Scenario", scenarioId));
        return execute(definition);
    }

    /** Runs a user-defined scenario through the same engine as the built-in ones. */
    @Transactional
    public RunDetail runCustom(CustomScenarioRequest request) {
        Agent agent = new Agent("agent-custom", request.name() + " Agent", request.archetype(),
                "User-defined custom scenario.", request.capabilities());
        List<AgentAction> actions = request.actions().stream().map(a -> a.toAction()).toList();
        ScenarioInfo info = new ScenarioInfo("custom", request.name(), "User-defined scenario.",
                "Explore how the engine evaluates a custom sequence of agent actions.",
                "User-defined", request.archetype(),
                "Outcomes depend on the chosen actions and the agent's capabilities.",
                agent.capabilities(), actions.size());
        return execute(new ScenarioDefinition(info, agent, actions));
    }

    @Transactional
    public RunDetail execute(ScenarioDefinition definition) {
        String runId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        List<Policy> policies = policyService.enabledPolicies();

        List<Event> events = new ArrayList<>();
        AgentState state = AgentState.INITIALIZING;
        int consecutiveBlocks = 0;
        int criticalBlocks = 0;
        RunStatus status = RunStatus.RUNNING;
        Instant timestamp = startedAt;

        for (AgentAction action : definition.actions()) {
            EvaluationRequest request = new EvaluationRequest(runId, definition.agent(), action,
                    List.copyOf(events), policies, state, consecutiveBlocks, timestamp);
            Event event = actionEvaluator.evaluate(request);
            events.add(event);

            state = event.agentStateAfter();
            boolean blocked = event.decision() == Decision.BLOCKED;
            consecutiveBlocks = blocked ? consecutiveBlocks + 1 : 0;
            if (blocked && event.riskBand() == RiskBand.CRITICAL) {
                criticalBlocks++;
            }
            timestamp = timestamp.plusMillis(ACTION_SPACING_MS);

            if (criticalBlocks >= riskProperties.getCriticalBlockTerminateThreshold()) {
                status = RunStatus.TERMINATED;
                break;
            }
        }

        if (status != RunStatus.TERMINATED) {
            status = RunStatus.COMPLETED;
        }
        AgentState finalState = status == RunStatus.TERMINATED ? AgentState.TERMINATED : state;
        Instant finishedAt = timestamp;

        SecurityReport report = reportService.generate(
                runId, definition.info(), definition.agent(), status, finalState, events);

        eventService.saveAll(events);
        runRepository.save(runMapper.toEntity(
                runId, definition.info(), definition.agent(), status, startedAt, finishedAt, events, report));

        return runService.detail(runId);
    }
}
