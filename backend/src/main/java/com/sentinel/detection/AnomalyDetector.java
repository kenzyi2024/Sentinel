package com.sentinel.detection;

import com.sentinel.config.AnomalyProperties;
import com.sentinel.domain.AgentAction;
import com.sentinel.domain.AnomalyResult;
import com.sentinel.domain.Event;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.ResourceSensitivity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Lightweight, deterministic behavioral anomaly detector. Given an agent's action history and the
 * action it is about to take, it produces an explainable anomaly score from three signals:
 *
 * <ol>
 *   <li><b>Bulk enumeration</b> — a sliding window of the last N actions; too many read/list
 *       actions inside it indicates the agent is sweeping the filesystem.</li>
 *   <li><b>Frequency spike (z-score)</b> — {@link WelfordVariance} builds a baseline of the
 *       read-count seen at each window position so far, and the current window's read-count is
 *       expressed as a number of standard deviations above that baseline.</li>
 *   <li><b>Sensitive-after-enumeration</b> — touching a sensitive/secret resource right after bulk
 *       enumeration is the textbook reconnaissance-then-collection sequence.</li>
 * </ol>
 *
 * <p>The detector is stateless: it recomputes from the supplied history on each call, which keeps
 * it trivially testable and free of per-run state to manage. Runs are short (tens of actions), so
 * the O(history × window) recomputation is negligible.
 */
@Component
public class AnomalyDetector {

    private static final Set<ActionType> READ_LIKE = EnumSet.of(
            ActionType.READ_FILE, ActionType.LIST_DIRECTORY, ActionType.SEARCH_FILES, ActionType.READ_ENV);

    private final AnomalyProperties props;

    public AnomalyDetector(AnomalyProperties props) {
        this.props = props;
    }

    public AnomalyResult analyze(List<Event> history, AgentAction current, ResourceSensitivity currentSensitivity) {
        int window = props.getWindowSize();

        List<ActionType> types = new ArrayList<>(history.size() + 1);
        for (Event event : history) {
            types.add(event.actionType());
        }
        types.add(current.type());
        int n = types.size();

        // Build a baseline of read-counts across every window position except the current one,
        // then measure how far the current window deviates from it.
        WelfordVariance baseline = new WelfordVariance();
        int currentWindowReads = 0;
        for (int i = 0; i < n; i++) {
            int windowStart = Math.max(0, i - window + 1);
            int reads = 0;
            for (int j = windowStart; j <= i; j++) {
                if (READ_LIKE.contains(types.get(j))) {
                    reads++;
                }
            }
            if (i < n - 1) {
                baseline.add(reads);
            } else {
                currentWindowReads = reads;
            }
        }

        int windowActionCount = Math.min(n, window);
        double zScore = baseline.count() >= 3 ? baseline.zScore(currentWindowReads) : 0.0;
        boolean enumerationDetected = currentWindowReads >= props.getEnumerationThreshold();
        boolean sensitive = currentSensitivity == ResourceSensitivity.SENSITIVE
                || currentSensitivity == ResourceSensitivity.SECRET;
        boolean sensitiveAfterEnumeration = sensitive && enumerationDetected && READ_LIKE.contains(current.type());

        List<String> reasons = new ArrayList<>();
        double score = 0.0;

        if (enumerationDetected) {
            double headroom = Math.max(1, window - props.getEnumerationThreshold());
            double over = clamp((currentWindowReads - props.getEnumerationThreshold()) / headroom);
            score = Math.max(score, clamp(0.5 + 0.5 * over));
            reasons.add(String.format(
                    "Bulk enumeration: %d read/list actions within the last %d (threshold %d).",
                    currentWindowReads, windowActionCount, props.getEnumerationThreshold()));
        }
        if (zScore > props.getZscoreAlert()) {
            score = Math.max(score, clamp(0.4 + (zScore - props.getZscoreAlert()) / (props.getZscoreAlert() * 3.0)));
            reasons.add(String.format(
                    "Read rate %.1fσ above the agent's established baseline (alert threshold %.1fσ).",
                    zScore, props.getZscoreAlert()));
        }
        if (sensitiveAfterEnumeration) {
            score = clamp(score + 0.3);
            reasons.add("A sensitive resource was accessed immediately after bulk file enumeration — "
                    + "a classic reconnaissance-then-collection pattern.");
        }

        return new AnomalyResult(clamp(score), zScore, windowActionCount,
                enumerationDetected, sensitiveAfterEnumeration, reasons);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
