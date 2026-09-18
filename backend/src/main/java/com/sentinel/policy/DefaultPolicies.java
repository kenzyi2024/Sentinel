package com.sentinel.policy;

import com.sentinel.domain.Policy;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.AgentArchetype;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.PolicyEffect;
import com.sentinel.domain.enums.ResourceSensitivity;

import java.util.List;

/**
 * The built-in policy set, seeded into the database on first startup and restorable via the API.
 * These encode the mitigations the design is built around — least privilege, human-in-the-loop for
 * risky effects, and supply-chain awareness — as data, not code.
 */
public final class DefaultPolicies {

    private DefaultPolicies() {
    }

    public static List<Policy> all() {
        return List.of(
                Policy.builder("policy-research-no-secrets")
                        .name("Research agents cannot access secrets")
                        .priority(100)
                        .archetypes(AgentArchetype.RESEARCH)
                        .minSensitivity(ResourceSensitivity.SECRET)
                        .effect(PolicyEffect.DENY)
                        .reason("Research agents gather information from documentation and code; they have no "
                                + "legitimate need for credentials (least privilege).")
                        .build(),
                Policy.builder("policy-delete-sensitive-denied")
                        .name("Deleting sensitive files is denied")
                        .priority(95)
                        .actionTypes(ActionType.DELETE_FILE)
                        .minSensitivity(ResourceSensitivity.SENSITIVE)
                        .effect(PolicyEffect.DENY)
                        .reason("Deletion of sensitive configuration or secret files is never permitted.")
                        .build(),
                Policy.builder("policy-secret-access-review")
                        .name("Access to secret resources is reviewed")
                        .priority(90)
                        .requiredCapability(Capability.READ_SECRETS)
                        .minSensitivity(ResourceSensitivity.SECRET)
                        .effect(PolicyEffect.REQUIRE_APPROVAL)
                        .reason("Any action touching SECRET material is held for human review, even for agents "
                                + "that hold the READ_SECRETS capability.")
                        .build(),
                Policy.builder("policy-egress-approval")
                        .name("Outbound network requires approval")
                        .priority(80)
                        .actionTypes(ActionType.NETWORK_REQUEST)
                        .effect(PolicyEffect.REQUIRE_APPROVAL)
                        .reason("Outbound network requests are a primary data-exfiltration channel and require "
                                + "human approval (human-in-the-loop).")
                        .build(),
                Policy.builder("policy-config-approval")
                        .name("Configuration changes require approval")
                        .priority(70)
                        .actionTypes(ActionType.MODIFY_CONFIG)
                        .effect(PolicyEffect.REQUIRE_APPROVAL)
                        .reason("Changes to configuration can alter the system's security posture and must be reviewed.")
                        .build(),
                Policy.builder("policy-install-warn")
                        .name("Dependency installs are logged for review")
                        .priority(40)
                        .actionTypes(ActionType.INSTALL_DEPENDENCY)
                        .effect(PolicyEffect.WARN)
                        .reason("Installing third-party dependencies is a supply-chain risk; surfaced for review.")
                        .build()
        );
    }
}
