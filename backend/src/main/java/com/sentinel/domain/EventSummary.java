package com.sentinel.domain;

import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.Decision;
import com.sentinel.domain.enums.RiskBand;

/**
 * A compact projection of an {@link Event} for list views and report highlights, where the full
 * factor/finding detail is not needed.
 */
public record EventSummary(
        String id,
        int sequence,
        ActionType actionType,
        String resource,
        Decision decision,
        int riskScore,
        RiskBand riskBand
) {
    public static EventSummary from(Event event) {
        return new EventSummary(
                event.id(),
                event.sequence(),
                event.actionType(),
                event.resource(),
                event.decision(),
                event.riskScore(),
                event.riskBand());
    }
}
