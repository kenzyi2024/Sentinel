package com.sentinel.domain;

import com.sentinel.domain.enums.InjectionCategory;
import com.sentinel.domain.enums.Severity;

/**
 * One prompt-injection signal found while scanning ingested text. Findings are structured (rule,
 * category, severity, matched snippet, position) so the UI can highlight exactly what tripped the
 * detector and why it matters.
 *
 * @param ruleId      the id of the detection rule that matched
 * @param category    the injection technique category
 * @param severity    how dangerous the pattern is
 * @param matchedText the offending snippet (already truncated for display)
 * @param position    character offset of the match within the scanned text
 * @param explanation human-readable justification
 */
public record InjectionFinding(
        String ruleId,
        InjectionCategory category,
        Severity severity,
        String matchedText,
        int position,
        String explanation
) {
}
