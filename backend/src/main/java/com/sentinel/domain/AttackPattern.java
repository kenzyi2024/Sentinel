package com.sentinel.domain;

import java.util.List;

/**
 * A multi-step attack pattern recognized across a run's event graph (e.g. reconnaissance leading
 * to sensitive-resource discovery leading to exfiltration). Surfaced in the security report.
 *
 * @param id             stable pattern identifier
 * @param name           short name
 * @param description    what the pattern means and why it is dangerous
 * @param eventSequences the sequence numbers of the events that make up the pattern, in order
 */
public record AttackPattern(
        String id,
        String name,
        String description,
        List<Integer> eventSequences
) {
    public AttackPattern {
        eventSequences = eventSequences == null ? List.of() : List.copyOf(eventSequences);
    }
}
