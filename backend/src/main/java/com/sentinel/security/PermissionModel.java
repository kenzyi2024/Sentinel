package com.sentinel.security;

import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.Capability;
import com.sentinel.domain.enums.ResourceSensitivity;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

/**
 * Maps an action (and the sensitivity of the resource it targets) to the set of capabilities it
 * requires. This is the single source of truth for "what authority does this action need", used
 * both by the permission checker and by scenario authoring.
 *
 * <p>Note the sensitivity-aware rule: reading an ordinary file needs only {@code READ_PROJECT},
 * but reading a file classified as {@code SECRET} additionally requires {@code READ_SECRETS}. This
 * is what makes an over-broad file read escalate into a least-privilege violation.
 */
@Component
public class PermissionModel {

    public Set<Capability> requiredCapabilities(ActionType type, ResourceSensitivity sensitivity) {
        EnumSet<Capability> required = EnumSet.noneOf(Capability.class);
        switch (type) {
            case LIST_DIRECTORY, SEARCH_FILES -> required.add(Capability.READ_PROJECT);
            case READ_FILE -> {
                required.add(Capability.READ_PROJECT);
                if (sensitivity == ResourceSensitivity.SECRET) {
                    required.add(Capability.READ_SECRETS);
                }
            }
            case READ_ENV -> required.add(Capability.READ_SECRETS);
            case WRITE_FILE -> required.add(Capability.WRITE_PROJECT);
            case MODIFY_CONFIG -> required.add(Capability.MODIFY_CONFIG);
            case DELETE_FILE -> required.add(Capability.DELETE_FILE);
            case INSTALL_DEPENDENCY -> required.add(Capability.INSTALL_DEPENDENCY);
            case EXECUTE_COMMAND, SPAWN_PROCESS -> required.add(Capability.EXECUTE_COMMAND);
            case NETWORK_REQUEST -> required.add(Capability.NETWORK_ACCESS);
        }
        return required;
    }
}
