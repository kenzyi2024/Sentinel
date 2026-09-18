package com.sentinel.evaluation;

import com.sentinel.domain.Agent;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.Event;
import com.sentinel.domain.Policy;
import com.sentinel.domain.enums.AgentState;

import java.time.Instant;
import java.util.List;

/**
 * All inputs the {@link ActionEvaluator} needs to evaluate a single action. Bundling them into a
 * record keeps the evaluator's signature stable and makes the call site (the simulation engine)
 * self-documenting.
 *
 * @param runId                 the run this action belongs to
 * @param agent                 the acting agent
 * @param action                the action being attempted
 * @param priorEvents           events already recorded in the run, in order (the action history)
 * @param policies              the active policy set to evaluate against
 * @param stateBefore           the agent's state before this action
 * @param consecutiveBlocksBefore how many actions have been blocked in a row up to now
 * @param timestamp             the timestamp to stamp on the resulting event
 */
public record EvaluationRequest(
        String runId,
        Agent agent,
        AgentAction action,
        List<Event> priorEvents,
        List<Policy> policies,
        AgentState stateBefore,
        int consecutiveBlocksBefore,
        Instant timestamp
) {
    public EvaluationRequest {
        priorEvents = priorEvents == null ? List.of() : List.copyOf(priorEvents);
        policies = policies == null ? List.of() : List.copyOf(policies);
    }
}
