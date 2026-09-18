package com.sentinel.evaluation;

import com.sentinel.detection.AnomalyDetector;
import com.sentinel.detection.CommandAnalysis;
import com.sentinel.detection.CommandAnalyzer;
import com.sentinel.detection.EscalationDetector;
import com.sentinel.detection.EscalationSignal;
import com.sentinel.detection.InjectionScanner;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.AnomalyResult;
import com.sentinel.domain.Event;
import com.sentinel.domain.InjectionFinding;
import com.sentinel.domain.PermissionCheck;
import com.sentinel.domain.PolicyDecision;
import com.sentinel.domain.RiskAssessment;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.AgentState;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.ResourceSensitivity;
import com.sentinel.risk.RiskEngine;
import com.sentinel.security.PermissionChecker;
import com.sentinel.security.PermissionModel;
import com.sentinel.security.ResourceClassifier;
import com.sentinel.simulation.AgentStateMachine;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The heart of Sentinel: the interception pipeline that turns an attempted {@link AgentAction} into
 * an evaluated {@link Event}. Every stage feeds the next, and the whole computation is deterministic.
 *
 * <pre>
 *   classify resource ─▶ check permissions ─▶ scan for injection ─▶ analyze command
 *        ─▶ detect anomaly ─▶ detect escalation ─▶ evaluate policy
 *        ─▶ score risk ─▶ resolve decision ─▶ build explanation ─▶ record event
 * </pre>
 *
 * <p>The evaluator holds no mutable state; the run's history is passed in on every call, which
 * keeps it thread-safe and directly unit-testable.
 */
@Component
public class ActionEvaluator {

    private final ResourceClassifier resourceClassifier;
    private final PermissionModel permissionModel;
    private final PermissionChecker permissionChecker;
    private final InjectionScanner injectionScanner;
    private final CommandAnalyzer commandAnalyzer;
    private final AnomalyDetector anomalyDetector;
    private final EscalationDetector escalationDetector;
    private final com.sentinel.policy.PolicyEngine policyEngine;
    private final RiskEngine riskEngine;
    private final DecisionResolver decisionResolver;
    private final ExplanationBuilder explanationBuilder;
    private final AgentStateMachine stateMachine;

    public ActionEvaluator(ResourceClassifier resourceClassifier,
                           PermissionModel permissionModel,
                           PermissionChecker permissionChecker,
                           InjectionScanner injectionScanner,
                           CommandAnalyzer commandAnalyzer,
                           AnomalyDetector anomalyDetector,
                           EscalationDetector escalationDetector,
                           com.sentinel.policy.PolicyEngine policyEngine,
                           RiskEngine riskEngine,
                           DecisionResolver decisionResolver,
                           ExplanationBuilder explanationBuilder,
                           AgentStateMachine stateMachine) {
        this.resourceClassifier = resourceClassifier;
        this.permissionModel = permissionModel;
        this.permissionChecker = permissionChecker;
        this.injectionScanner = injectionScanner;
        this.commandAnalyzer = commandAnalyzer;
        this.anomalyDetector = anomalyDetector;
        this.escalationDetector = escalationDetector;
        this.policyEngine = policyEngine;
        this.riskEngine = riskEngine;
        this.decisionResolver = decisionResolver;
        this.explanationBuilder = explanationBuilder;
        this.stateMachine = stateMachine;
    }

    public Event evaluate(EvaluationRequest request) {
        AgentAction action = request.action();

        ResourceSensitivity sensitivity = resourceClassifier.classify(action.resource());
        Set<Capability> required = permissionModel.requiredCapabilities(action.type(), sensitivity);
        PermissionCheck permissionCheck = permissionChecker.check(request.agent(), required);
        List<InjectionFinding> injectionFindings = injectionScanner.scan(action.scannableText());
        CommandAnalysis commandAnalysis = isCommand(action.type())
                ? commandAnalyzer.analyze(action.command())
                : CommandAnalysis.safe();
        AnomalyResult anomalyResult = anomalyDetector.analyze(request.priorEvents(), action, sensitivity);
        EscalationSignal escalationSignal = escalationDetector.analyze(request.priorEvents(), action, sensitivity);
        PolicyDecision policyDecision = policyEngine.evaluate(
                request.agent(), action, sensitivity, required, request.policies());

        EvaluationContext context = new EvaluationContext(
                request.agent(), action, sensitivity, required, permissionCheck,
                injectionFindings, commandAnalysis, anomalyResult, escalationSignal,
                policyDecision, request.priorEvents());

        RiskAssessment assessment = riskEngine.assess(context);
        Resolution resolution = decisionResolver.resolve(
                permissionCheck, policyDecision, assessment, commandAnalysis.danger());
        Explanation explanation = explanationBuilder.explain(context, assessment, resolution);

        boolean ingestedInjection = !injectionFindings.isEmpty() && isIngest(action.type());
        int consecutiveBlocks = resolution.decision() == Decision.BLOCKED
                ? request.consecutiveBlocksBefore() + 1
                : 0;
        AgentState stateAfter = stateMachine.next(request.stateBefore(), ingestedInjection, consecutiveBlocks);

        int sequence = request.priorEvents().size() + 1;
        String previousEventId = request.priorEvents().isEmpty()
                ? null
                : request.priorEvents().get(request.priorEvents().size() - 1).id();

        return Event.builder()
                .id(UUID.randomUUID().toString())
                .runId(request.runId())
                .sequence(sequence)
                .timestamp(request.timestamp())
                .agentId(request.agent().id())
                .agentName(request.agent().name())
                .actionType(action.type())
                .resource(action.resource())
                .command(action.command())
                .intent(action.intent())
                .parameters(action.parameters())
                .requiredCapabilities(required)
                .missingCapabilities(permissionCheck.missing())
                .resourceSensitivity(sensitivity)
                .riskAssessment(assessment)
                .injectionFindings(injectionFindings)
                .anomalyResult(anomalyResult)
                .policyDecision(policyDecision)
                .decision(resolution.decision())
                .executed(resolution.executed())
                .reasons(explanation.reasons())
                .warnings(resolution.warnings())
                .recommendations(explanation.recommendations())
                .previousEventId(previousEventId)
                .agentStateAfter(stateAfter)
                .inducedByInjection(action.inducedByInjection())
                .build();
    }

    private static boolean isCommand(ActionType type) {
        return type == ActionType.EXECUTE_COMMAND || type == ActionType.SPAWN_PROCESS;
    }

    private static boolean isIngest(ActionType type) {
        return type == ActionType.READ_FILE || type == ActionType.READ_ENV || type == ActionType.SEARCH_FILES;
    }
}
