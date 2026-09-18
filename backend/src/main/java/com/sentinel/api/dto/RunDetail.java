package com.sentinel.api.dto;

import com.sentinel.domain.Event;
import com.sentinel.domain.SecurityReport;

import java.util.List;

/**
 * A run with its full event stream and security report — the payload behind the Run Explorer.
 */
public record RunDetail(
        RunSummary summary,
        List<Event> events,
        SecurityReport report
) {
    public RunDetail {
        events = events == null ? List.of() : List.copyOf(events);
    }
}
