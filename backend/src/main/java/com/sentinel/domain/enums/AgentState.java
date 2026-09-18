package com.sentinel.domain.enums;

/**
 * Lifecycle state of an agent during a run, modeled as a small state machine.
 *
 * <pre>
 *   INITIALIZING ──▶ ACTIVE ──▶ COMPROMISED ──▶ QUARANTINED ──▶ TERMINATED
 *                      │             │                              ▲
 *                      └─────────────┴──────────────────────────────┘
 * </pre>
 *
 * An agent becomes {@code COMPROMISED} when it ingests content that the injection detector
 * flags; it is {@code QUARANTINED} when repeated blocks indicate hostile behavior, and
 * {@code TERMINATED} when the run's kill-switch trips.
 */
public enum AgentState {
    INITIALIZING,
    ACTIVE,
    COMPROMISED,
    QUARANTINED,
    TERMINATED
}
