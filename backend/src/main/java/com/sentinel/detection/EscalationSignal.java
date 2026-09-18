package com.sentinel.detection;

import java.util.List;

/**
 * The escalation detector's verdict on a single action, given the run so far: a normalized signal
 * in [0, 1] and the reasons that produced it.
 */
public record EscalationSignal(double signal, List<String> reasons) {
    public EscalationSignal {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static EscalationSignal none() {
        return new EscalationSignal(0.0, List.of());
    }
}
