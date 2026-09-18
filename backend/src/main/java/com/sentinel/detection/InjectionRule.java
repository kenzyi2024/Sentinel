package com.sentinel.detection;

import com.sentinel.domain.enums.InjectionCategory;
import com.sentinel.domain.enums.Severity;

import java.util.regex.Pattern;

/**
 * One prompt-injection detection rule: a compiled pattern plus the metadata used to explain a
 * match. Rules are data — the {@link PatternInjectionScanner} holds an ordered library of them, so
 * extending coverage means adding a rule, not editing detection logic.
 *
 * @param id          stable rule identifier (e.g. {@code override.ignore-previous})
 * @param category    the injection technique this rule detects
 * @param severity    how dangerous a match is
 * @param pattern     the compiled regular expression
 * @param description human-readable explanation of what the rule catches
 */
public record InjectionRule(
        String id,
        InjectionCategory category,
        Severity severity,
        Pattern pattern,
        String description
) {
    public static InjectionRule of(String id, InjectionCategory category, Severity severity,
                                    String regex, String description) {
        return new InjectionRule(id, category, severity,
                Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL), description);
    }
}
