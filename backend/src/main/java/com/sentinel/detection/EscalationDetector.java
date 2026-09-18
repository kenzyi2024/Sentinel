package com.sentinel.detection;

import com.sentinel.domain.AgentAction;
import com.sentinel.domain.AttackPattern;
import com.sentinel.domain.Event;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.ResourceSensitivity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Detects privilege-escalation and multi-step attack chains by treating a run's events as a
 * directed graph (each event links to its predecessor via {@code previousEventId}) and traversing
 * it.
 *
 * <p>Each action is classified into an attack <b>phase</b>:
 * {@code RECON → DISCOVERY → ESCALATION / EXFILTRATION}. Isolated actions are rarely alarming; it
 * is the <em>ordering</em> that reveals intent. Walking the predecessor chain backward from the
 * latest event recovers the phase sequence, so an outbound request that <em>follows</em> secret
 * discovery which <em>followed</em> reconnaissance scores far higher than the same request in
 * isolation. Post-injection actions are treated as compromised behavior.
 */
@Component
public class EscalationDetector {

    private enum Phase { NONE, RECON, DISCOVERY, ESCALATION, EXFILTRATION, OTHER }

    private final CommandAnalyzer commandAnalyzer;

    public EscalationDetector(CommandAnalyzer commandAnalyzer) {
        this.commandAnalyzer = commandAnalyzer;
    }

    /**
     * Scores the current action against the run's history.
     *
     * @param priorEvents        events already recorded in this run, in chronological order
     * @param current            the action being evaluated
     * @param currentSensitivity sensitivity of the resource {@code current} targets
     */
    public EscalationSignal analyze(List<Event> priorEvents, AgentAction current, ResourceSensitivity currentSensitivity) {
        Phase currentPhase = classify(current.type(), currentSensitivity, current.command());
        List<Phase> chain = phaseChain(priorEvents);

        boolean sawRecon = chain.contains(Phase.RECON);
        boolean sawDiscovery = chain.contains(Phase.DISCOVERY);
        boolean sawInjection = priorEvents.stream().anyMatch(Event::hasInjection);
        boolean postCompromise = sawInjection || current.inducedByInjection();

        double signal = 0.0;
        List<String> reasons = new ArrayList<>();

        if (currentPhase == Phase.EXFILTRATION) {
            if (sawRecon && sawDiscovery) {
                signal = Math.max(signal, 1.0);
                reasons.add("Completes a reconnaissance → sensitive-discovery → exfiltration chain.");
            } else if (sawDiscovery) {
                signal = Math.max(signal, 0.85);
                reasons.add("Outbound/exfiltration action following sensitive-resource discovery.");
            } else {
                signal = Math.max(signal, 0.5);
                reasons.add("Outbound action attempted with no legitimate prior context in this run.");
            }
        }
        if (currentPhase == Phase.ESCALATION) {
            signal = Math.max(signal, 0.55);
            reasons.add("Privilege-escalation or persistence action.");
            if (sawRecon || sawDiscovery) {
                signal = Math.max(signal, 0.75);
                reasons.add("Escalation attempt follows earlier reconnaissance or discovery.");
            }
        }
        if (currentPhase == Phase.DISCOVERY && sawRecon) {
            signal = Math.max(signal, 0.55);
            reasons.add("Sensitive-resource discovery immediately following reconnaissance.");
        }
        if (postCompromise
                && (current.type().isMutating() || currentPhase == Phase.EXFILTRATION || currentPhase == Phase.DISCOVERY)) {
            signal = Math.max(signal, 0.7);
            reasons.add("Action taken after the agent ingested a prompt-injection payload (post-compromise behavior).");
        }

        return new EscalationSignal(clamp(signal), reasons);
    }

    /**
     * Identifies the completed attack patterns present across all of a run's events (used by the
     * security report).
     */
    public List<AttackPattern> detectPatterns(List<Event> events) {
        List<AttackPattern> patterns = new ArrayList<>();
        if (events == null || events.isEmpty()) {
            return patterns;
        }

        Integer firstRecon = null;
        Integer firstDiscovery = null;
        Integer firstEscalation = null;
        Integer firstExfil = null;
        Integer firstInjection = null;
        List<Integer> reconSeq = new ArrayList<>();
        List<Integer> discoverySeq = new ArrayList<>();
        List<Integer> escalationSeq = new ArrayList<>();
        List<Integer> exfilSeq = new ArrayList<>();
        List<Integer> enumerationSeq = new ArrayList<>();
        List<Integer> inducedSeq = new ArrayList<>();

        for (Event event : events) {
            Phase phase = classify(event.actionType(), event.resourceSensitivity(), event.command());
            switch (phase) {
                case RECON -> { reconSeq.add(event.sequence()); if (firstRecon == null) firstRecon = event.sequence(); }
                case DISCOVERY -> { discoverySeq.add(event.sequence()); if (firstDiscovery == null) firstDiscovery = event.sequence(); }
                case ESCALATION -> { escalationSeq.add(event.sequence()); if (firstEscalation == null) firstEscalation = event.sequence(); }
                case EXFILTRATION -> { exfilSeq.add(event.sequence()); if (firstExfil == null) firstExfil = event.sequence(); }
                default -> { /* NONE / OTHER */ }
            }
            if (event.hasInjection() && firstInjection == null) {
                firstInjection = event.sequence();
            }
            if (event.inducedByInjection()) {
                inducedSeq.add(event.sequence());
            }
            if (event.anomalyResult().enumerationDetected()) {
                enumerationSeq.add(event.sequence());
            }
        }

        if (firstRecon != null && firstDiscovery != null && firstExfil != null
                && firstRecon < firstDiscovery && firstDiscovery < firstExfil) {
            List<Integer> chain = new ArrayList<>();
            chain.add(firstRecon);
            chain.add(firstDiscovery);
            chain.add(firstExfil);
            patterns.add(new AttackPattern("chain.recon-discovery-exfil",
                    "Reconnaissance → Discovery → Exfiltration",
                    "The agent enumerated the project, located sensitive material, then attempted to send data "
                            + "outbound — the canonical data-theft kill chain.", chain));
        }
        if (firstInjection != null && !inducedSeq.isEmpty()) {
            List<Integer> chain = new ArrayList<>();
            chain.add(firstInjection);
            chain.addAll(inducedSeq);
            patterns.add(new AttackPattern("chain.injection-induced",
                    "Prompt-injection-induced action chain",
                    "The agent ingested content containing a prompt-injection payload and then performed actions "
                            + "consistent with obeying it, rather than its declared goal.", chain));
        }
        if (!escalationSeq.isEmpty()) {
            patterns.add(new AttackPattern("pattern.privilege-escalation",
                    "Privilege escalation / persistence attempt",
                    "The agent attempted to elevate privileges, alter configuration, or establish persistence.",
                    escalationSeq));
        }
        if (!discoverySeq.isEmpty()) {
            patterns.add(new AttackPattern("pattern.secret-access",
                    "Sensitive-resource access attempt",
                    "The agent attempted to read secrets, credentials, or otherwise sensitive resources.",
                    discoverySeq));
        }
        if (!enumerationSeq.isEmpty()) {
            patterns.add(new AttackPattern("pattern.bulk-enumeration",
                    "Anomalous bulk enumeration",
                    "The agent read or listed files at a rate well above its established baseline.",
                    enumerationSeq));
        }
        return patterns;
    }

    /** Recovers the chronological phase sequence by walking the event graph's predecessor links. */
    private List<Phase> phaseChain(List<Event> priorEvents) {
        if (priorEvents == null || priorEvents.isEmpty()) {
            return List.of();
        }
        Map<String, Event> byId = new HashMap<>();
        Event last = priorEvents.get(0);
        for (Event event : priorEvents) {
            byId.put(event.id(), event);
            if (event.sequence() >= last.sequence()) {
                last = event;
            }
        }
        LinkedList<Phase> phases = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Event cursor = last;
        while (cursor != null && visited.add(cursor.id())) {
            phases.addFirst(classify(cursor.actionType(), cursor.resourceSensitivity(), cursor.command()));
            cursor = cursor.previousEventId() != null ? byId.get(cursor.previousEventId()) : null;
        }
        return phases;
    }

    private Phase classify(ActionType type, ResourceSensitivity sensitivity, String command) {
        return switch (type) {
            case NETWORK_REQUEST -> Phase.EXFILTRATION;
            case EXECUTE_COMMAND, SPAWN_PROCESS -> {
                CommandAnalysis analysis = commandAnalyzer.analyze(command);
                if (analysis.exfiltration()) {
                    yield Phase.EXFILTRATION;
                }
                if (analysis.escalation()) {
                    yield Phase.ESCALATION;
                }
                yield Phase.OTHER;
            }
            case MODIFY_CONFIG, DELETE_FILE -> Phase.ESCALATION;
            case READ_ENV -> Phase.DISCOVERY;
            case READ_FILE -> sensitivity == ResourceSensitivity.SECRET ? Phase.DISCOVERY : Phase.NONE;
            case LIST_DIRECTORY, SEARCH_FILES -> Phase.RECON;
            default -> Phase.NONE;
        };
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
