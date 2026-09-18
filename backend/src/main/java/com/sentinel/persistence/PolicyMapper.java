package com.sentinel.persistence;

import com.sentinel.domain.Policy;
import com.sentinel.domain.enums.ActionType;
import com.sentinel.domain.enums.AgentArchetype;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps between the domain {@link Policy} and its {@link PolicyEntity} persistence view, translating
 * the archetype and action-type sets to and from comma-separated enum names.
 */
@Component
public class PolicyMapper {

    public PolicyEntity toEntity(Policy policy) {
        PolicyEntity entity = new PolicyEntity();
        entity.setId(policy.id());
        entity.setName(policy.name());
        entity.setPriority(policy.priority());
        entity.setArchetypes(joinNames(policy.archetypes()));
        entity.setActionTypes(joinNames(policy.actionTypes()));
        entity.setResourcePattern(policy.resourcePattern());
        entity.setMinSensitivity(policy.minSensitivity());
        entity.setRequiredCapability(policy.requiredCapability());
        entity.setEffect(policy.effect());
        entity.setReason(policy.reason());
        entity.setEnabled(policy.enabled());
        return entity;
    }

    public Policy toDomain(PolicyEntity entity) {
        Set<AgentArchetype> archetypes = parse(entity.getArchetypes(), AgentArchetype::valueOf);
        Set<ActionType> actionTypes = parse(entity.getActionTypes(), ActionType::valueOf);
        return new Policy(
                entity.getId(),
                entity.getName(),
                entity.getPriority(),
                archetypes,
                actionTypes,
                entity.getResourcePattern(),
                entity.getMinSensitivity(),
                entity.getRequiredCapability(),
                entity.getEffect(),
                entity.getReason(),
                entity.isEnabled());
    }

    private static <E extends Enum<E>> String joinNames(Set<E> values) {
        return values.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private static <E extends Enum<E>> Set<E> parse(String csv, java.util.function.Function<String, E> factory) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(factory)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
