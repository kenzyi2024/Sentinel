package com.sentinel.domain;

/**
 * A concrete, safer action the agent could take instead of the one that was blocked or held for
 * review — plus the rationale. Unlike the operator-facing recommendations (which harden policy),
 * this is agent-facing: "do this instead." Produced deterministically by {@code SafeAlternativeAdvisor}.
 *
 * @param action    the safer action to take instead
 * @param rationale why it is safer
 */
public record SafeAlternative(String action, String rationale) {
}
