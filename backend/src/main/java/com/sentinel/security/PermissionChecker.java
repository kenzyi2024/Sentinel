package com.sentinel.security;

import com.sentinel.domain.Agent;
import com.sentinel.domain.PermissionCheck;
import com.sentinel.domain.enums.Capability;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Checks a set of required capabilities against what an agent has been granted, producing an
 * explainable {@link PermissionCheck}. This is the enforcement point for least privilege.
 */
@Component
public class PermissionChecker {

    public PermissionCheck check(Agent agent, Set<Capability> required) {
        Set<Capability> granted = agent.capabilities();
        Set<Capability> missing = new LinkedHashSet<>();
        for (Capability capability : required) {
            if (!granted.contains(capability)) {
                missing.add(capability);
            }
        }
        return new PermissionCheck(required, granted, missing);
    }
}
