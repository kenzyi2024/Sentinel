package com.sentinel.detection;

import com.sentinel.domain.InjectionFinding;

import java.util.List;

/**
 * Scans a block of ingested text for prompt-injection signals.
 *
 * <p>This interface is the extension seam of the detection subsystem. The shipped implementation
 * ({@link PatternInjectionScanner}) is deterministic and rule-based — it is an educational /
 * security-analysis engine, <em>not</em> a production LLM-grade detector. A future machine-learning
 * classifier could implement this same interface and be dropped in without touching the risk
 * engine, policy engine, or evaluation pipeline.
 */
public interface InjectionScanner {

    /**
     * @param text ingested content (a file's contents, a command, a document) to inspect
     * @return the injection findings, in order of appearance; empty if nothing suspicious was found
     */
    List<InjectionFinding> scan(String text);
}
