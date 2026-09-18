package com.sentinel.domain.enums;

/**
 * Lifecycle status of a simulation run.
 */
public enum RunStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    /** Halted early by the kill-switch after too many critical blocked actions. */
    TERMINATED,
    FAILED
}
