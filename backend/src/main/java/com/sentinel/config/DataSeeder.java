package com.sentinel.config;

import com.sentinel.policy.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds the built-in default policies into the database on startup (unless disabled via
 * {@code sentinel.seed-on-startup=false}), so a freshly-cloned checkout launches straight into a
 * usable, policy-enforcing demo.
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final PolicyService policyService;
    private final boolean seedOnStartup;

    public DataSeeder(PolicyService policyService,
                      @Value("${sentinel.seed-on-startup:true}") boolean seedOnStartup) {
        this.policyService = policyService;
        this.seedOnStartup = seedOnStartup;
    }

    @Override
    public void run(String... args) {
        if (seedOnStartup) {
            policyService.seedDefaultsIfEmpty();
            log.info("Sentinel ready — {} active policies loaded.", policyService.findAll().size());
        }
    }
}
