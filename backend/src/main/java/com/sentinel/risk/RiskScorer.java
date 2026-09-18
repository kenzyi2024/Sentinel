package com.sentinel.risk;

import com.sentinel.domain.RiskFactor;
import com.sentinel.domain.enums.RiskCategory;
import com.sentinel.evaluation.EvaluationContext;

import java.util.Optional;

/**
 * A single, independent dimension of risk. Each scorer inspects the {@link EvaluationContext} and
 * either contributes one explainable {@link RiskFactor} or abstains ({@link Optional#empty()}).
 *
 * <p>The {@link RiskEngine} discovers all scorer beans and sums their contributions, so adding a
 * new risk signal is purely additive: implement this interface, annotate it as a component, and it
 * participates automatically — no change to the engine, the evaluator, or existing scorers. This is
 * the modularity the design calls for, and it is why the score is never a single hard-coded number.
 */
public interface RiskScorer {

    /** The risk dimension this scorer contributes to. */
    RiskCategory category();

    /** @return a factor to add to the score, or empty if this dimension does not apply. */
    Optional<RiskFactor> score(EvaluationContext context);
}
