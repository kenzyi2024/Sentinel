package com.sentinel.repository;

import com.sentinel.persistence.RunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data repository for simulation runs.
 */
public interface RunRepository extends JpaRepository<RunEntity, String> {

    List<RunEntity> findAllByOrderByStartedAtDesc();
}
