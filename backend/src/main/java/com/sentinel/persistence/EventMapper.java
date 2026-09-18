package com.sentinel.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.domain.Event;
import org.springframework.stereotype.Component;

/**
 * Maps between the domain {@link Event} and its {@link EventEntity} persistence view. The full
 * event is serialized to JSON for storage and deserialized on read, so the rich structure survives
 * a round-trip intact; the promoted columns are derived from the event on write.
 */
@Component
public class EventMapper {

    private final ObjectMapper objectMapper;

    public EventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EventEntity toEntity(Event event) {
        EventEntity entity = new EventEntity();
        entity.setId(event.id());
        entity.setRunId(event.runId());
        entity.setSequence(event.sequence());
        entity.setTimestamp(event.timestamp());
        entity.setActionType(event.actionType());
        entity.setResource(truncate(event.resource(), 1024));
        entity.setDecision(event.decision());
        entity.setRiskScore(event.riskScore());
        entity.setRiskBand(event.riskBand());
        entity.setPreviousEventId(event.previousEventId());
        entity.setDetailsJson(write(event));
        return entity;
    }

    public Event toDomain(EventEntity entity) {
        try {
            return objectMapper.readValue(entity.getDetailsJson(), Event.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize event " + entity.getId(), e);
        }
    }

    private String write(Event event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event " + event.id(), e);
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
