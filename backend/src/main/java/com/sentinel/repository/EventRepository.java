package com.sentinel.repository;

import com.sentinel.persistence.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data repository for persisted events.
 */
public interface EventRepository extends JpaRepository<EventEntity, String> {

    List<EventEntity> findByRunIdOrderBySequenceAsc(String runId);

    long countByRunId(String runId);

    void deleteByRunId(String runId);
}
