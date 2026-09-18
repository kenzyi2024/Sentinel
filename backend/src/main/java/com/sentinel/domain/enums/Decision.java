package com.sentinel.domain.enums;

/**
 * The final outcome of evaluating an action, after combining the permission check, policy
 * evaluation, and risk assessment. This is what the event stream and timeline display.
 */
public enum Decision {
    /** The action proceeded (possibly with warnings attached). */
    ALLOWED,
    /** The action was held for a human decision because risk or policy required approval. */
    REQUIRES_APPROVAL,
    /** The action was blocked and never executed. */
    BLOCKED
}
