package com.sentinel.telemetry;

import com.sentinel.domain.Event;
import com.sentinel.persistence.EventMapper;
import com.sentinel.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Persists and retrieves telemetry events. Events are the immutable record of every evaluated
 * action; the timeline and execution graph in the UI are reconstructed from them (each event links
 * to its predecessor via {@code previousEventId}).
 */
@Service
public class EventService {

    private final EventRepository repository;
    private final EventMapper mapper;

    public EventService(EventRepository repository, EventMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public void saveAll(List<Event> events) {
        repository.saveAll(events.stream().map(mapper::toEntity).toList());
    }

    @Transactional(readOnly = true)
    public List<Event> findByRun(String runId) {
        return repository.findByRunIdOrderBySequenceAsc(runId).stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public long countByRun(String runId) {
        return repository.countByRunId(runId);
    }

    @Transactional(readOnly = true)
    public long totalEvents() {
        return repository.count();
    }
}
