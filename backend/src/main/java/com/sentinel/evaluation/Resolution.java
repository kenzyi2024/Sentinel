package com.sentinel.evaluation;

import com.sentinel.domain.enums.Decision;

import java.util.List;

/**
 * The resolved outcome of an action: the final {@link Decision}, whether it was executed, and any
 * non-blocking warnings attached.
 */
public record Resolution(Decision decision, boolean executed, List<String> warnings) {
    public Resolution {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
