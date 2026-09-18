package com.sentinel.simulation;

import com.sentinel.domain.enums.AgentState;
import org.springframework.stereotype.Component;

/**
 * The agent lifecycle state machine. Transitions are a pure function of the current state, whether
 * the agent just ingested injected content, and how many actions it has had blocked in a row:
 *
 * <pre>
 *   INITIALIZING ─▶ ACTIVE ─(ingests injection)─▶ COMPROMISED
 *        ACTIVE / COMPROMISED ─(2+ consecutive blocks)─▶ QUARANTINED
 *        QUARANTINED / TERMINATED are terminal here (TERMINATED is set at run level).
 * </pre>
 */
@Component
public class AgentStateMachine {

    private static final int QUARANTINE_AFTER_CONSECUTIVE_BLOCKS = 2;

    public AgentState next(AgentState current, boolean ingestedInjection, int consecutiveBlocks) {
        if (current == AgentState.TERMINATED || current == AgentState.QUARANTINED) {
            return current;
        }
        AgentState state = current == AgentState.INITIALIZING ? AgentState.ACTIVE : current;
        if (ingestedInjection && state == AgentState.ACTIVE) {
            state = AgentState.COMPROMISED;
        }
        if (consecutiveBlocks >= QUARANTINE_AFTER_CONSECUTIVE_BLOCKS
                && (state == AgentState.ACTIVE || state == AgentState.COMPROMISED)) {
            state = AgentState.QUARANTINED;
        }
        return state;
    }
}
