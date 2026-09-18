package com.sentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for Sentinel — a deterministic, local-only engine that runs simulated AI agents
 * through security scenarios, evaluates every action they attempt, and explains each decision.
 *
 * <p>There is no external LLM or cloud dependency: all intelligence (risk scoring, policy
 * evaluation, prompt-injection detection, anomaly detection) is implemented in this codebase.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SentinelApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelApplication.class, args);
    }
}
